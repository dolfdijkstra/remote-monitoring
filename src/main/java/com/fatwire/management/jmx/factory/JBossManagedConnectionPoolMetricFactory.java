package com.fatwire.management.jmx.factory;


public class JBossManagedConnectionPoolMetricFactory extends
        XmlMetricFactory {
    /**
     * 
        ObjectName = jboss.jca:service=ManagedConnectionPool,name=csDataSource org.jboss.resource.connectionmanager.JBossManagedConnectionPool
        BlockingTimeoutMillis int 30000
        BackGroundValidationMinutes long 10
        AvailableConnectionCount long 49
        ConnectionCount int 10
        MinSize int 10
        MaxConnectionsInUseCount long 3
        MaxSize int 50
        InUseConnectionCount long 1
        ConnectionCreatedCount int 10
        ConnectionDestroyedCount int 0
        IdleTimeoutMinutes long 0

     * 
     * 
     */
    /*
    @Override
    public Map<String, Type> createDef() {
        Map<String, Type> def = new HashMap<String, Type>();
        def.put("AvailableConnectionCount", Type.GAUGE);
        def.put("ConnectionCount", Type.GAUGE);
        def.put("MaxConnectionsInUseCount", Type.GAUGE);
        def.put("InUseConnectionCount", Type.GAUGE);
        return def;
    }

    @Override
    public ObjectName getQuery() throws MalformedObjectNameException,
            NullPointerException {
        return new ObjectName("*:service=ManagedConnectionPool,*");
    }

    @Override
    protected String getDesciption(ObjectName objName) {
        return safeString("ManagedConnectionPool", objName
                .getKeyProperty("name"));
    }
    */
}
