package com.fatwire.management.jmx;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.management.ObjectName;

import com.fatwire.management.JMXMetric;
import com.fatwire.management.domain.Host;
import com.fatwire.management.jmx.factory.JMXMetricFactory;
import com.fatwire.management.jmx.factory.XmlMetricFactory;

public class MetricDiscoverer {
    interface DiscovererListener {

        void onDiscoveryStart(Host host);

        void onDiscoveryEnd(Host host);

        void onMetricFoundEvent(MetricFoundEvent e);
    }

    private XmlMetricFactory[] factories;

    private final Host host;

    private Set<DiscovererListener> listeners = new java.util.concurrent.CopyOnWriteArraySet<DiscovererListener>();

    public MetricDiscoverer(File[] factories, Host host) {
        this.factories = createFactories(factories).toArray(
                new XmlMetricFactory[0]);
        this.host = host;
    }

    public void addListener(DiscovererListener l) {
        this.listeners.add(l);
    }

    public void removeListener(DiscovererListener l) {
        this.listeners.remove(l);
    }

    public Set<ObjectName> discoverNames(RemoteConnection connection)
            throws IOException {
        return new TreeSet<ObjectName>(connection.getServer().queryNames(null,
                null));

    }

    public void discover(RemoteConnection connection) {
        this.fireStart(connection.getHost());
        try {
            for (XmlMetricFactory factory : factories) {
                int i = 0;
                for (JMXMetric m : factory.create(connection)) {
                    i++;
                    MetricFoundEvent event = new MetricFoundEvent(this, host, m);
                    fire(event);
                }
                if (i == 0) {
                    System.err.println(factory + ": no metrics found.");
                }
            }
        } finally {
            this.fireEnd(connection.getHost());
        }

    }

    List<JMXMetricFactory> createFactories(File[] factories) {
        final List<JMXMetricFactory> list = new LinkedList<JMXMetricFactory>();
        for (File f : factories) {
            if (f.exists()) {
                XmlMetricFactory factory = new XmlMetricFactory();
                factory.setXmlFile(f.getAbsoluteFile());
                list.add(factory);
            }
        }
        return list;

    }

    private void fire(final MetricFoundEvent event) {
        for (DiscovererListener l : this.listeners) {
            l.onMetricFoundEvent(event);
        }

    }

    private void fireStart(final Host host) {
        for (DiscovererListener l : this.listeners) {
            l.onDiscoveryStart(host);
        }

    }

    private void fireEnd(final Host host) {
        for (DiscovererListener l : this.listeners) {
            l.onDiscoveryEnd(host);
        }

    }

}
