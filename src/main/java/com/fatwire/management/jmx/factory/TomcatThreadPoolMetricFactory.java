package com.fatwire.management.jmx.factory;

import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.fatwire.management.jmx.AttributeDescriptor;
import com.fatwire.management.jmx.ObjectAttribute.Type;

public class TomcatThreadPoolMetricFactory extends TomcatMetricFactory {

    @Override
    public Map<String, AttributeDescriptor> createDef() {
        Map<String, AttributeDescriptor> def = new HashMap<String, AttributeDescriptor>();
        def.put("currentThreadsBusy", new AttributeDescriptor(
                "currentThreadsBusy", Type.GAUGE, "currentThreadsBusy"));
        def.put("currentThreadCount", new AttributeDescriptor(
                "currentThreadCount", Type.GAUGE, "currentThreadCount"));
        def.put("maxThreads", new AttributeDescriptor("maxThreads", Type.GAUGE,
                "maxThreads"));

        return def;
    }

    @Override
    public ObjectName getQuery() throws MalformedObjectNameException,
            NullPointerException {
        return new ObjectName(domain + ":type=ThreadPool,*");
    }

    @Override
    protected String getDesciption(ObjectName objName) {
        return safeString("ThreadPool", objName.getKeyProperty("name"));
    }
}
