package com.fatwire.management.jmx;

import com.fatwire.management.JMXMetric;
import com.fatwire.management.domain.Host;

public class MetricFoundEvent {

    private final Host jmxHost;

    private final JMXMetric metric;

    private final MetricDiscoverer source;

    /**
     * @param jmxHost
     * @param metric
     * @param source
     */
    public MetricFoundEvent(MetricDiscoverer source, Host jmxHost, JMXMetric metric) {
        super();
        this.jmxHost = jmxHost;
        this.metric = metric;
        this.source = source;
    }

    /**
     * @return the jmxHost
     */
    public Host getJmxHost() {
        return jmxHost;
    }

    /**
     * @return the metric
     */
    public JMXMetric getMetric() {
        return metric;
    }

    /**
     * @return the source
     */
    public MetricDiscoverer getSource() {
        return source;
    }

}
