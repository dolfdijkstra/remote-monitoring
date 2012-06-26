package com.fatwire.management.jmx;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class TomcatServletModelDataFactory implements ModelDataFactory<Float> {

    @Override
    public List<ModelData<Float>> create(final MBeanServerConnection server,
            ObjectName query) {
        ObjectName poolName = null;

        try {
            List<ModelData<Float>> data = new LinkedList<ModelData<Float>>();
            Set<ObjectName> mbeans = server.queryNames(query, null);
            if (mbeans != null) {
                for (ObjectName objName : mbeans) {
                    data.add(new MBeanCounterData(server,objName, "requestCount"));

                }
            }
            return data;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
