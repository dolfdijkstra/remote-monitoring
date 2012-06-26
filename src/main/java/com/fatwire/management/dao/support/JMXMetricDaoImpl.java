package com.fatwire.management.dao.support;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;

import com.fatwire.management.JMXMetric;
import com.fatwire.management.dao.JMXMetricDao;
import com.fatwire.management.domain.Host;

public class JMXMetricDaoImpl implements JMXMetricDao {

    Map<Host, Set<JMXMetric>> metrics = new HashMap<Host, Set<JMXMetric>>();

    @Override
    public void addJMXMetric(JMXMetric metric) {
        Host host = metric.getHost();
        Set<JMXMetric> s = getMetricsForHost(host);
        if (s == null) {
            s = new HashSet<JMXMetric>();
            metrics.put(host, s);
        }
        s.add(metric);
    }

    @Override
    public void deleteJMXMetric(JMXMetric metric) {
        Host host = metric.getHost();
        Set<JMXMetric> s = getMetricsForHost(host);
        if (s != null) {
            s.remove(metric);
        }

    }

    @Override
    public Collection<JMXMetric> queryMetrics(Host host) {
        Set<JMXMetric> s = getMetricsForHost(host);
        if (s == null)
            return Collections.emptySet();
        return Collections.unmodifiableCollection(s);
    }

    private Set<JMXMetric> getMetricsForHost(Host host) {
        return metrics.get(host);
    }


    public JMXMetric findMetric(Host host, ObjectName objectName) {
        for (JMXMetric m : queryMetrics(host)) {
            if (objectName.equals(m.getObjName())) {
                return m;
            }

        }
        return null;
    }

}
