package com.fatwire.management.jmx.factory;


public class TomcatDataJspMonitorMetricFactory extends XmlMetricFactory {
/*
    public Map<String, Type> createDef() {
        Map<String, Type> def = new HashMap<String, Type>();
        def.put("jspCount", Type.COUNTER);
        def.put("jspReloadCount", Type.COUNTER);
        return def;
    }

    @Override
    public ObjectName getQuery() throws MalformedObjectNameException,
            NullPointerException {
        return new ObjectName(domain + ":type=JspMonitor,*");
    }
    @Override
    protected String getDesciption(ObjectName objName) {
        return this.safeString("JspMonitor", objName.getKeyProperty("name"),objName.getKeyProperty("WebModule"));
    }
 */   
}
