package com.fatwire.management.jmx.factory;


public class TomcatManagerMetricFactory extends XmlMetricFactory {
/*
    @Override
    public Map<String, Type> createDef() {
        Map<String, Type> def = new HashMap<String, Type>();
        def.put("activeSessions", Type.GAUGE);
        def.put("sessionCounter", Type.COUNTER);
        def.put("maxActive", Type.GAUGE);
        def.put("sessionAverageAliveTime", Type.GAUGE);
        return def;
    }

    @Override
    public ObjectName getQuery() throws MalformedObjectNameException,
            NullPointerException {
        return new ObjectName(domain + ":type=Manager,*");
    }

    @Override
    protected String getDesciption(ObjectName objName) {
        return safeString("SessionManager", objName.getKeyProperty("host"),
                objName.getKeyProperty("path"));
    }
    */
}
