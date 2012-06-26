package com.fatwire.management.rrd;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fatwire.management.JMXMetric;
import com.fatwire.management.jmx.Main;

public class MetricGrapher {
    private static final Logger log = LoggerFactory.getLogger(MetricGrapher.class);

    private final File path;

    private Map<File, ReentrantLock> locks = new ConcurrentHashMap<File, ReentrantLock>();

    public MetricGrapher(File path) {
        this.path = path;
    }

    public void graph(JMXMetric metric, long start, long end)
            throws IOException {

        File x = new File(path, RrdUtil.toRrdName(metric) + ".rrd");
        int i = 0;
        for (URL template : metric.getGraphTemplateUrls()) {
            String suffix = (i == 0) ? "" : "-" + Integer.toString(i);
            File graphName = new File(path, RrdUtil.toRrdName(metric) + suffix
                    + ".png");
            String title = metric.getObjName().toString();

            graph(x, graphName, start, end, template, title);
            i++;
        }
    }

    Lock getLock(File name) {
        ReentrantLock lock = locks.get(name);
        if (lock == null) {
            lock = new ReentrantLock();
            locks.put(name, lock);
        }
        return lock;

    }

    public void graph(File rrdFile, File graphName, long intervalStart,
            long intervalEnd, URL templateUrl, String title) {
        Lock lock = getLock(graphName);
        if (lock.tryLock()) {
            try {
                Map<String, String> map = new HashMap<String, String>();
                map.put("imagetitle", title);
                log.debug("graphing " + rrdFile.getName() + " to "
                        + graphName.getName());
                new Rrd4jUtil().createGraph(templateUrl, rrdFile, graphName,
                        intervalStart, intervalEnd, map);

            } finally {
                lock.unlock();
            }
        }
    }

}
