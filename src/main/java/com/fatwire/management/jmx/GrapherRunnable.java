package com.fatwire.management.jmx;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.rrd4j.core.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fatwire.management.JMXMetric;
import com.fatwire.management.rrd.MetricGrapher;

public class GrapherRunnable implements Runnable {
    private static final Logger log = LoggerFactory
            .getLogger(GrapherRunnable.class);

    private boolean running = false;

    private final JMXMetric metric;

    private final MetricGrapher grapher;

    private final long start;

    private final SummaryStatistics graphStat;

    /**
     * @param metric
     * @param grapher
     * @param start
     * @param graphStat
     */
    public GrapherRunnable(JMXMetric metric, MetricGrapher grapher, long start,
            SummaryStatistics graphStat) {
        super();
        this.metric = metric;
        this.grapher = grapher;
        this.start = start;
        this.graphStat = graphStat;
    }

    public void run() {
        if (running) {
            return;
        }
        try {
            running = true;
            final long end = Util.getTimestamp();
            final long t = System.nanoTime();
            grapher.graph(metric, start, end);
            final long e = System.nanoTime() - t;
            graphStat.addValue(e / 1000000);
        } catch (final Exception e1) {
            log.error(metric.getDescription(), e1);

        } finally {
            running = false;
        }

    }

}
