package com.fatwire.management.jmx;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;


/**
 * 
 * To be used if the jmx value is a increasing counter (number of hits)
 * 
 * @author Dolf.Dijkstra
 * @since May 15, 2009
 */
public class MBeanCounterData implements ModelData<Float> {
    private String name;

    private final MBeanServerConnection server;

    private ObjectName objName;

    private String attribute;

    float max = 0;

    float old = 0;

    float used = 0;

    private History<Float> history = new History<Float>(100000);

    public MBeanCounterData(final MBeanServerConnection server, ObjectName name,
            String attribute) throws Exception {
        super();
        this.server = server;
        this.objName = name;
        this.attribute = attribute;
        this.name = name.getKeyProperty("name") != null ? name
                .getKeyProperty("name")
                + " " + attribute : attribute;

        Object s = server.getAttribute(objName, attribute);
        //System.out.println(s.getClass());
        float v = (Integer) s;
        old = v;

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
    public void refreshData() {
        try {
            Object s = server.getAttribute(objName, attribute);
            float v = (Integer) s;
            float v2 = v - old;
            old = v;
            max = Math.max(v2, max);
            used = v2;
            history.add(v2);
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (MBeanException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public History<Float> getHistory() {
        return history;
    }

}
