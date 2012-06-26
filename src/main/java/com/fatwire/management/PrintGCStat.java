package com.fatwire.management;

import static java.lang.management.ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE;
import static java.lang.management.ManagementFactory.MEMORY_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE;
import static java.lang.management.ManagementFactory.RUNTIME_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.getGarbageCollectorMXBeans;
import static java.lang.management.ManagementFactory.getMemoryMXBean;
import static java.lang.management.ManagementFactory.getMemoryPoolMXBeans;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Example of using the java.lang.management API to monitor 
 * the memory usage and garbage collection statistics.
 *
 * @author  Mandy Chung
 * @version %% 07/27/04
 */
public class PrintGCStat {
    private RuntimeMXBean rmbean;

    private MemoryMXBean mmbean;

    private List<MemoryPoolMXBean> pools;

    private List<GarbageCollectorMXBean> gcmbeans;

    /**
     * Constructs a PrintGCStat object to monitor a remote JVM.
     */
    public void connect() throws IOException {
        // Create the platform mxbean proxies
        this.rmbean = newPlatformMXBeanProxy(server, RUNTIME_MXBEAN_NAME,
                RuntimeMXBean.class);
        this.mmbean = newPlatformMXBeanProxy(server, MEMORY_MXBEAN_NAME,
                MemoryMXBean.class);
        ObjectName poolName = null;
        ObjectName gcName = null;
        try {
            poolName = new ObjectName(MEMORY_POOL_MXBEAN_DOMAIN_TYPE + ",*");
            gcName = new ObjectName(GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
        } catch (MalformedObjectNameException e) {
            // should not reach here
            assert (false);
        }

        Set mbeans = server.queryNames(poolName, null);
        if (mbeans != null) {
            pools = new ArrayList<MemoryPoolMXBean>();
            Iterator iterator = mbeans.iterator();
            while (iterator.hasNext()) {
                ObjectName objName = (ObjectName) iterator.next();
                MemoryPoolMXBean p = newPlatformMXBeanProxy(server, objName
                        .getCanonicalName(), MemoryPoolMXBean.class);
                pools.add(p);
            }
        }

        mbeans = server.queryNames(gcName, null);
        if (mbeans != null) {
            gcmbeans = new ArrayList<GarbageCollectorMXBean>();
            Iterator iterator = mbeans.iterator();
            while (iterator.hasNext()) {
                ObjectName objName = (ObjectName) iterator.next();
                GarbageCollectorMXBean gc = newPlatformMXBeanProxy(server,
                        objName.getCanonicalName(),
                        GarbageCollectorMXBean.class);
                gcmbeans.add(gc);
            }
        }
    }

    /**
     * Constructs a PrintGCStat object to monitor the local JVM.
     */
    public void connectLocal() {
        // Obtain the platform mxbean instances for the running JVM.
        this.rmbean = getRuntimeMXBean();
        this.mmbean = getMemoryMXBean();
        this.pools = getMemoryPoolMXBeans();
        this.gcmbeans = getGarbageCollectorMXBeans();
    }

    /**
     * Prints the verbose GC log to System.out to list the memory usage
     * of all memory pools as well as the GC statistics. 
     */
    public void printVerboseGc() {
        System.out.print("Uptime: " + formatMillis(rmbean.getUptime()));
        for (GarbageCollectorMXBean gc : gcmbeans) {
            System.out.print(" [" + gc.getName() + ": ");
            System.out.print("Count=" + gc.getCollectionCount());
            System.out.print(" GCTime=" + formatMillis(gc.getCollectionTime()));
            System.out.print("]");
        }
        System.out.println();
        for (MemoryPoolMXBean p : pools) {
            System.out.print("  [" + p.getName() + " ");
            System.out.print("  (" + p.getType() + "):");
            MemoryUsage u = p.getUsage();
            System.out.print(" Init=" + formatBytes(u.getInit()));
            System.out.print(" Used=" + formatBytes(u.getUsed()));
            System.out.print(" Committed=" + formatBytes(u.getCommitted()));
            System.out.print(" Max=" + formatBytes(u.getMax()));
            System.out.println("]");
        }
    }

    private String formatMillis(long ms) {
        return String.format("%.4fsec", ms / (double) 1000);
    }

    private String formatBytes(long bytes) {
        long kb = bytes;
        if (bytes > 0) {
            kb = bytes / 1024;
        }
        return kb + "K";
    }

    public static void main(String[] args) throws Exception {
        long interval = 5000; // default is 5 second interval
        long mins = 1;
        long samples = (mins * 60 * 1000) / interval;
        String hostName = "radium";
        int portNum = 8087;

        //JMXServiceURL u = new JMXServiceURL("service:jmx:rmi:///jndi//rmi://"
        //                + hostName + ":" + portNum + "/jmxrmi");

        //JMXConnector c = JMXConnectorFactory.connect(u);
        //c.connect();
        //MBeanServerConnection server = c.getMBeanServerConnection();
        
        PrintGCStat pstat = new PrintGCStat();
        pstat.connect(hostName, portNum);
        pstat.connect();
        for (int i = 0; i < samples; i++) {
            pstat.printVerboseGc();
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                System.exit(1);
            }
        }
        pstat.close();

    }

    private void close() throws IOException {
        this.jmxc.close();

    }

    private MBeanServerConnection server;

    private JMXConnector jmxc;

    public void connect(String hostname, int port) {
        System.out.println("Connecting to " + hostname + ":" + port);

        // Create an RMI connector client and connect it to
        // the RMI connector server
        String urlPath = "/jndi/rmi://" + hostname + ":" + port + "/jmxrmi";
        connect(urlPath);
    }

    /**
     * Connect to a JMX agent of a given URL. 
     */
    private void connect(String urlPath) {
        try {
            JMXServiceURL url = new JMXServiceURL("rmi", "", 0, urlPath);
            this.jmxc = JMXConnectorFactory.connect(url);
            this.server = jmxc.getMBeanServerConnection();
        } catch (MalformedURLException e) {
            // should not reach here
        } catch (IOException e) {
            System.err.println("\nCommunication error: " + e.getMessage());
            System.exit(1);
        }
    }
}
