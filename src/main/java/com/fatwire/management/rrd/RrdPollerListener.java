package com.fatwire.management.rrd;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;

import org.rrd4j.core.RrdDb;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fatwire.management.JMXMetric;
import com.fatwire.management.jmx.PollEvent;
import com.fatwire.management.jmx.PollerListener;

public class RrdPollerListener implements PollerListener {

    private long step = 1;

    private File rrdFileName;

    private JMXMetric metric;

    private RrdDb rrdDb;

    private static final Logger log = LoggerFactory
            .getLogger(RrdPollerListener.class);

    public RrdPollerListener(File path, JMXMetric metric, long step)
            throws IOException {
        this.metric = metric;
        this.step = step;

        rrdFileName = new File(path, RrdUtil.toRrdName(metric) + ".rrd");
        rrdFileName.getParentFile().mkdirs();
        if (!rrdFileName.exists()) {
            createRrd();
        }

    }

    private void createRrd() {

        Map<String, String> map = new HashMap<String, String>();
        map.put("start", Long.toString(Util.getTimestamp(new Date()) - 10));
        map.put("step", Long.toString(step));

        new Rrd4jUtil().createRrdDb(metric.getDbTemplateUrl(),
                this.rrdFileName, map);
    }

    public void open() throws IOException {
        rrdDb = new RrdDb(rrdFileName.getAbsolutePath());
    }

    public void close() {
         
        if (rrdDb != null) {
            try {
                rrdDb.close();
            } catch (Exception e) {
                log.error(String.valueOf(rrdDb), e);

            }
        }

    }

    @Override
    public void onPollEvent(PollEvent event) {
        if (!event.getMetric().equals(this.metric))
            return;
        if (rrdDb == null || rrdDb.isClosed())
            return;
        try {
            long t = System.nanoTime();
            Sample sample = rrdDb.createSample();
            sample.setTime(event.getTimeStamp() / 1000);

            for (Attribute v : event.getAttributeValues()) {
                String dsn = metric.getObjectAttribute(v.getName())
                        .getDatasourceName();
                sample.setValue(dsn, toDouble(v.getValue()));
            }
            sample.update();
            long t1 = System.nanoTime();
            log.debug("updating took " + (t1 - t) / 1000 + " us for "
                    + metric.getObjName());
        } catch (Throwable e) {
            log.error(e.getMessage() + " for " + metric.getObjName() + " on "
                    + this.rrdFileName, e);
        }

    }

    private double toDouble(Object value) {
        double d;
        if (value instanceof Number) {
            Number n = (Number) value;
            d = n.doubleValue();
        } else {
            d = Double.valueOf(String.valueOf(value));
            log.debug(d + " from " + value.getClass().getName());
        }
        if (d < 0) {
            log.warn("value is " + d);
            return 0D;
        }
        return d;
    }

    /**
     * @return the rrdFileName
     */
    public File getRrdFileName() {
        return rrdFileName;
    }

}
