package com.fatwire.management.jmx;

import java.util.ArrayList;
import java.util.List;

import javax.management.Attribute;

import com.fatwire.management.JMXMetric;

public class PollEvent {

    private final JMXMetric metric;

    private final long timeStamp;

    private List<Attribute> attributeValues = new ArrayList<Attribute>();

    /**
     * @param reference
     * @param value
     * @param timeStamp
     */
    public PollEvent(JMXMetric reference, long timeStamp) {
        super();
        this.metric = reference;

        this.timeStamp = timeStamp;
    }

    public void add(Iterable<Attribute> attributes) {
        for (Attribute a : attributes) {
            attributeValues.add(a);
        }

    }

    public void registerException(Exception e) {
        e.printStackTrace(); //TODO handle properly
    }

    /**
     * @return the metric
     */
    public JMXMetric getMetric() {
        return metric;
    }

    /**
     * @return the timeStamp in milliseconds
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * @return the attributeValues
     */
    public List<Attribute> getAttributeValues() {
        return attributeValues;
    }
}
