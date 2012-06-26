package com.fatwire.management.jmx.factory;

import java.util.List;

import com.fatwire.management.JMXMetric;
import com.fatwire.management.jmx.RemoteConnection;

public interface JMXMetricFactory {

    public List<JMXMetric> create(final RemoteConnection server);

    /**
     * @param filter the filter to set
     */
    public void setFilter(ObjectNameFilter filter);

}