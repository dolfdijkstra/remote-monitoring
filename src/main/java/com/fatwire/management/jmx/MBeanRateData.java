package com.fatwire.management.jmx;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class MBeanRateData implements ModelData<Float> {
    private String name;

    private final MBeanServerConnection server;

    private ObjectName objName;

    private String attribute;

    float max = 0;

    float used = 0;

    private History<Float> history = new History<Float>(100000);

    public MBeanRateData(final MBeanServerConnection server,
            ObjectName name, String attribute) throws Exception {
        super();
        this.server = server;
        this.objName = name;
        this.attribute = attribute;
        this.name = name.getKeyProperty("name") != null ? name
                .getKeyProperty("name")
                + " " + attribute : attribute;

    }

    @Override
    public Float getMax() {
        return max;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Float getValue() {
        return used;
    }

    @Override
    public void refreshData() throws Exception {
        Object s = server.getAttribute(objName, attribute);
        float v = (Integer) s;
        max = Math.max(v, max);
        used = v;
        history.add(used);

    }

    @Override
    public History<Float> getHistory() {
        return history ;
    }

}
