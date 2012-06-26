package com.fatwire.management.jmx;

import java.util.HashSet;
import java.util.Set;

import javax.management.AttributeList;
import javax.management.MBeanServerConnection;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fatwire.management.JMXMetric;

public class PollerRunnable implements Runnable {

    private final RemoteConnection server;

    private static final Logger log = LoggerFactory
            .getLogger(PollerRunnable.class);

    private final JMXMetric metric;
    final SummaryStatistics pollStat;
    private final Set<PollerListener> listeners = new HashSet<PollerListener>();

    private long lastPoll = System.currentTimeMillis() - 1000;

    /**
     * @param server
     * @param metric
     */
    public PollerRunnable(RemoteConnection server, JMXMetric metric,final SummaryStatistics pollStat) {
        super();
        this.server = server;
        this.metric = metric;
        this.pollStat=pollStat;
    }

    public void addListener(final PollerListener listener) {
        listeners.add(listener);
    }

    @Override
    public void run() {
        if (listeners.isEmpty()) {
            return;
        }
        if (System.currentTimeMillis() - lastPoll < 1000) {
            return;
        }
        final long now = System.currentTimeMillis();
        lastPoll = now;

        final PollEvent event = new PollEvent(metric, now);
        long t =  System.nanoTime();
        try {
            AttributeList al;
            al = getServer().getAttributes(metric.getObjName(),
                    metric.getAttributes());
            event.add(al.asList());

        } catch (final Exception e) {
            log.error(metric.getDescription(), e);
            event.registerException(e);
        }
        long t1 = 0;
        if (log.isDebugEnabled() ) {
            t1 = System.nanoTime();
            log.debug("reading jmx data took {} us for {}", (t1 - t) / 1000,
                    metric.getObjName());
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
            log.debug("notifying listeners took {} us for {}",
                    (t2 - t1) / 1000, metric.getObjName());
        }
        
        this.pollStat.addValue((System.nanoTime()-t)/1000);    
        

    }

    /**
     * @return the server
     */
    public MBeanServerConnection getServer() {
        return server.getServer();
    }

}
