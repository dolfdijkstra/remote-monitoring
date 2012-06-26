package com.fatwire.management.jmx;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.log4j.xml.DOMConfigurator;
import org.rrd4j.core.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fatwire.management.dao.HostDao;
import com.fatwire.management.dao.JMXMetricDao;
import com.fatwire.management.dao.support.HostDaoImpl;
import com.fatwire.management.dao.support.JMXMetricDaoImpl;
import com.fatwire.management.domain.Host;
import com.fatwire.management.jetty.HostController;
import com.fatwire.management.jetty.ImageController;
import com.fatwire.management.jetty.IndexController;
import com.fatwire.management.jetty.JettyService;
import com.fatwire.management.jetty.ShutdownController;
import com.fatwire.management.jmx.MetricDiscoverer.DiscovererListener;
import com.fatwire.management.rrd.MetricGrapher;
import com.fatwire.management.rrd.RrdPollerListener;

public class Main {
    private HostDao hostDao;

    private JMXMetricDao metricDao;

    private File basePath = new File("./rrd");

    final CountDownLatch latch = new CountDownLatch(1);

    private JettyService service;

    private ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(
            2);

    private ThreadPoolExecutor grapherExecutorService = new ThreadPoolExecutor(
            20, 20, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    private ExecutorService pollerExecutorService = java.util.concurrent.Executors
            .newFixedThreadPool(20);

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    Main() {
        hostDao = new HostDaoImpl();
        metricDao = new JMXMetricDaoImpl();
    }

    void bootJetty() throws Exception {

        service = new JettyService();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    latch.countDown();
                } catch (final Exception e) {
                    e.printStackTrace();
                }

            }

        }));

        IndexController indexController = new IndexController();
        indexController.setHostDao(hostDao);
        indexController.setMetricDao(this.metricDao);
        service.addController(indexController, "/");

        HostController c = new HostController();
        c.setHostDao(hostDao);
        c.setMetricDao(this.metricDao);
        service.addController(c, "/host/*");
        ImageController imageController = new ImageController();
        imageController.setDir(basePath.toURI());
        imageController.setHostDao(hostDao);
        imageController.setMetricDao(this.metricDao);
        service.addController(imageController, "*.png");

        ShutdownController sds = new ShutdownController();
        sds.setLatch(latch);
        service.addController(sds, "/shutdown");

        service.init(8080);

    }

    /**
     * @param args
     */
    public static void main(final String[] args) throws Exception {
        if (new File("conf/log4j.xml").exists()) {
            DOMConfigurator.configure("conf/log4j.xml");
        }
        /**
         * processes
         * 1) discoverer
         * 2) poller
         * 3) grapher
         * 4) jetty
         * 
         */

        final String h = "10.133.40.37";
        final InetAddress address = InetAddress.getByName(h);

        final int port = 8087;// 12345;
        Host host = new Host();
        host.setAddress(address);
        host.setJmxPort(port);

        Main main = new Main();
        main.bootJetty();
        main.run(host);

    }

    //        SnmpConnection snmpCon = new SnmpConnection();
    //        snmpCon.connect("udp:10.133.40.37/161");
    //        SnmpPoller sp = new SnmpPoller();
    //        sp.setConnection(snmpCon);
    //        sp.poll();
    //        snmpCon.close();
    //OID avg1 = new OID("1.3.6.1.4.1.2021.10.1.3.2");
    //System.out.println(avg1.predecessor());
    //System.out.println(avg1.nextPeer());
    //System.exit(0);
    //connection.connect("10.133.40.37", 8087);

    private void run(final Host host) throws Exception, IOException,
            InterruptedException {
        hostDao.addHost(host);
        final long step = 5;//time in seconds between polls
        final File path = new File(this.basePath, host.getAddress()
                .getHostAddress()
                + "_" + host.getJmxPort() + "/");
        path.mkdirs();
        final int runLength = 15; //time to collect the data 
        final int graphStep = 30; //
        File[] factories = new File("conf/template/factory/").listFiles(new FilenameFilter(){

            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".xml");
            }});

        final RemoteConnection connection = new RemoteConnection();

        connection.connect(host);


        final MetricDiscoverer discoverer = new MetricDiscoverer(factories,
                host);
        long t1 = System.nanoTime();
        Set<ObjectName> names = discoverer.discoverNames(connection);
        for (ObjectName name : names) {

        }

        long t2 = System.nanoTime();
        System.out
                .println("discovering names tool " + (t2 - t1) / 1000 + " us");

        final SummaryStatistics pollStat = new SummaryStatistics();
        final SummaryStatistics graphStat = new SummaryStatistics();

        final List<RrdPollerListener> listeners = new ArrayList<RrdPollerListener>();
        final long start = Util.getTimestamp();
        final MetricGrapher grapher = new MetricGrapher(path);
        final DiscovererListener listener = new DiscovererListener() {

            @Override
            public void onDiscoveryEnd(final Host host) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onDiscoveryStart(final Host host) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onMetricFoundEvent(final MetricFoundEvent e) {

                metricDao.addJMXMetric(e.getMetric());
                log.info("{} found metric {}", e.getJmxHost(), e.getMetric()
                        .getOid());

                try {
                    final RrdPollerListener l = new RrdPollerListener(path, e
                            .getMetric(), step);
                    l.open();
                    listeners.add(l);
                    final PollerRunnable callable = new PollerRunnable(
                            connection, e.getMetric(), pollStat);
                    callable.addListener(l);
                    scheduler.scheduleAtFixedRate(new Runnable() {

                        @Override
                        public void run() {
                            pollerExecutorService.execute(callable);

                        }
                    }, 0, step, TimeUnit.SECONDS);
                    final GrapherRunnable gr = new GrapherRunnable(e
                            .getMetric(), grapher, start, graphStat);
                    scheduler.scheduleAtFixedRate(new Runnable() {

                        @Override
                        public void run() {
                            grapherExecutorService.execute(gr);
                        }
                    }, graphStep, graphStep, TimeUnit.SECONDS); //print a graph each minute

                } catch (IOException e1) {
                    log.error(e1.getMessage(), e1);

                }

            }

        };
        discoverer.addListener(listener);

        discoverer.discover(connection);

        latch.await(runLength * 60, TimeUnit.SECONDS);
        log.debug("TaskCount in scheduler is {}", scheduler.getTaskCount());
        for (Runnable r : scheduler.getQueue()) {
            r.run();//one more time
        }
        scheduler.shutdownNow();
        pollerExecutorService.shutdown();
        grapherExecutorService.shutdown();
        pollerExecutorService.awaitTermination(60, TimeUnit.SECONDS);
        for (final RrdPollerListener l : listeners) {
            l.close();
        }

        grapherExecutorService.awaitTermination(60, TimeUnit.SECONDS);
        connection.close();
        System.out.println("an average poll lasted " + pollStat.getMean()
                + " ms");
        System.out.println("an average graph lasted " + graphStat.getMean()
                + " ms");
        service.shutdown();

        //poller.shutdown();
    }
}
