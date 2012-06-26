package com.fatwire.management.jmx.factory;


public class TomcatDataSourceMetricFactory extends XmlMetricFactory {
/*
    @Override
    public Map<String, Type> createDef() {
        Map<String, Type> def = new HashMap<String, Type>();
        def.put("numIdle", Type.GAUGE);
        def.put("numActive", Type.GAUGE);
        return def;
    }

    @Override
    public ObjectName getQuery() throws MalformedObjectNameException,
            NullPointerException {
        return new ObjectName(domain + ":type=DataSource,*");
    }
    @Override
    protected String getDesciption(ObjectName objName) {
        return safeString("DataSource" , objName.getKeyProperty("name"));
    }
*/
}
