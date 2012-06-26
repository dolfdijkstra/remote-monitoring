package com.fatwire.management.jmx;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ReflectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fatwire.management.JMXMetric;

public class Poller {

    private RemoteConnection server;

    private static final Logger log = LoggerFactory.getLogger(Poller.class);

    private final List<JMXMetric> dataToPoll = new LinkedList<JMXMetric>();

    private final Set<PollerListener> listeners = new HashSet<PollerListener>();

    private long lastPoll = System.currentTimeMillis() - 1000;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void addListener(final PollerListener listener) {
        listeners.add(listener);
    }

    public void addManagementDataReference(final JMXMetric metric) {
        synchronized (dataToPoll) {
            dataToPoll.add(metric);
        }
    }

    public void clear() {
        dataToPoll.clear();
    }

    public boolean poll() {
        if (listeners.isEmpty()) {
            return false;
        }
        if (System.currentTimeMillis() - lastPoll < 1000) {
            return false;
        }
        final long now = System.currentTimeMillis();
        lastPoll = now;
        Collection<JMXMetric> col = null;
        synchronized (dataToPoll) {
            col = Collections.unmodifiableCollection(dataToPoll);
        }
        List<Future<PollEvent>> futures = new LinkedList<Future<PollEvent>>();
        for (final JMXMetric metric : col) {

            Callable<PollEvent> r = new Callable<PollEvent>() {

                @Override
                public PollEvent call() throws Exception {

                    final PollEvent event = new PollEvent(metric, now);
                    long t = log.isDebugEnabled() ? System.nanoTime() : 0;
                    try {
                        AttributeList al;
                        al = getServer().getAttributes(metric.getObjName(),
                                metric.getAttributes());
                        event.add(al.asList());

                    } catch (final InstanceNotFoundException e) {
                        log.error(metric.getDescription(), e);
                        event.registerException(e);
                    } catch (final ReflectionException e) {
                        log.error(metric.getDescription(), e);
                        event.registerException(e);
                    } catch (final IOException e) {
                        log.error(metric.getDescription(), e);
                        event.registerException(e);
                    }
                    long t1 = 0;
                    if (t > 0) {
                        t1 = System.nanoTime();
                        log.debug("reading jmx data took " + (t1 - t) / 1000
                                + " us for " + metric.getObjName());
                    }
                    for (final PollerListener l : listeners) {
                        try {
                            l.onPollEvent(event);
                        } catch (final Throwable e) {
                            log.error(metric.getDescription(), e);
                        }
                    }
                    if (t1 > 0) {
                        long t2 = System.nanoTime();
                        log.debug("notifying listeners took " + (t2 - t1)
                                / 1000 + " us for " + metric.getObjName());
                    }
                    return event;
                }

            };

            this.executorService.submit(r);
        }
        for (Future<PollEvent> f : futures) {
            try {
                f.get();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);

            } catch (ExecutionException e) {
                log.error(e.getMessage(), e);

            }
        }
        return true;
    }

    /**
     * @return the server
     */
    public MBeanServerConnection getServer() {
        return server.getServer();
    }

    /**
     * @param server the server to set
     */
    public void setServer(final RemoteConnection server) {
        this.server = server;
    }

    /**
     * @return the dataToPoll
     */
    public Collection<JMXMetric> getDataToPoll() {
        return Collections.unmodifiableList(dataToPoll);
    }

    public void shutdown() {
        this.executorService.shutdown();
    }
}
