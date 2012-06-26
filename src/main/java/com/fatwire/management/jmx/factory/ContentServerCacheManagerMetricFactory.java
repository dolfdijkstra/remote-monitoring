package com.fatwire.management.jmx.factory;


public class ContentServerCacheManagerMetricFactory extends
        XmlMetricFactory {
    /*
    public Map<String, Type> createDef() {
        Map<String, Type> def = new HashMap<String, Type>();
        //ObjectName = com.fatwire.cs:type=CacheManager,name="SystemLocaleString:locale" com.fatwire.cs.profiling.jmx.CacheStats
        //LastPrunedDate java.util.Date
        def.put("Hits", Type.COUNTER);// long 11
        def.put("Misses", Type.COUNTER); //long 2
        def.put("Size", Type.GAUGE);// long 1
        def.put("ClearCount", Type.COUNTER);// long 0
        //CreatedDate java.util.Date
        def.put("RemoveCount", Type.COUNTER);// long 0
        //LastFlushedDate java.util.Date

        return def;

    }

    @Override
    public ObjectName getQuery() throws MalformedObjectNameException,
            NullPointerException {
        return new ObjectName("com.fatwire.cs:type=Cache,*");
    }

    @Override
    protected String getDesciption(ObjectName objName) {
        //com.fatwire.cs:type=CacheManager,name="SystemLocaleString:locale"
        String name = objName.getKeyProperty("name");
        return safeString("ContentServer-Cache-", name);
    }
    */

}
