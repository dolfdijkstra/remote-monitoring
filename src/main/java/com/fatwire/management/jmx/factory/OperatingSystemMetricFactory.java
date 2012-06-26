package com.fatwire.management.jmx.factory;


public class OperatingSystemMetricFactory extends XmlMetricFactory {
/*
    public Map<String, Type> createDef() {
        Map<String, Type> def = new HashMap<String, Type>();
        //def.put("AvailableProcessors", Type.GAUGE);
        
        //def.put("MaxFileDescriptorCount", Type.GAUGE);
        def.put("FreePhysicalMemorySize", Type.GAUGE);
        def.put("CommittedVirtualMemorySize", Type.GAUGE);
        def.put("TotalPhysicalMemorySize", Type.GAUGE);
        def.put("TotalSwapSpaceSize", Type.GAUGE);
        def.put("FreeSwapSpaceSize", Type.GAUGE);
        def.put("ProcessCpuTime", Type.COUNTER);
        def.put("OpenFileDescriptorCount", Type.GAUGE);
        def.put("SystemLoadAverage", Type.GAUGE);
        return def;



    }

    @Override
    public ObjectName getQuery() throws MalformedObjectNameException,
            NullPointerException {
        return new ObjectName("java.lang:type=OperatingSystem");
    }
    @Override
    protected String getDesciption(ObjectName objName) {
        return "OperatingSystem";
    }
*/
}
