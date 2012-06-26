package com.fatwire.management.jmx.factory;


public class TomcatServletMetricFactory extends XmlMetricFactory {
/*
    @Override
    public Map<String, Type> createDef() {
        Map<String, Type> def = new HashMap<String, Type>();
        def.put("processingTime", Type.COUNTER);
        def.put("maxTime", Type.GAUGE);
        def.put("minTime", Type.GAUGE);
        def.put("requestCount", Type.COUNTER);
        def.put("errorCount", Type.COUNTER);
        return def;
    }

    @Override
    public ObjectName getQuery() throws MalformedObjectNameException,
            NullPointerException {
        return new ObjectName("*:j2eeType=Servlet,*");
    }

    @Override
    protected String getDesciption(ObjectName objName) {
        return safeString("Servlet", objName.getKeyProperty("name"), objName
                .getKeyProperty("WebModule"));
    }
*/
}
