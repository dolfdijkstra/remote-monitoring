package com.fatwire.management.dao;

import java.util.Collection;

import javax.management.ObjectName;

import com.fatwire.management.JMXMetric;
import com.fatwire.management.domain.Host;

public interface JMXMetricDao {

    Collection<JMXMetric> queryMetrics(Host host);

    JMXMetric findMetric(Host host, ObjectName objectName);

    void addJMXMetric(JMXMetric metric);

    void deleteJMXMetric(JMXMetric metric);

}
