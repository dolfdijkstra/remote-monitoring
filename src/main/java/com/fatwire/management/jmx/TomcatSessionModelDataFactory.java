package com.fatwire.management.jmx;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class TomcatSessionModelDataFactory implements ModelDataFactory<Float> {

    @Override
    public List<ModelData<Float>> create(final MBeanServerConnection server,
            ObjectName query) {
        //ObjectName = Catalina:type=Manager,path=/cs,host=localhost org.apache.commons.modeler.BaseModelMBean
        /*
        maxActiveSessions int -1
        maxInactiveInterval int 1800
        processExpiresFrequency int 6
        activeSessions int 2
        sessionCounter int 9
        maxActive int 4
        sessionMaxAliveTime int 24811
        sessionAverageAliveTime int 12205
*/

        try {
            List<ModelData<Float>> data = new LinkedList<ModelData<Float>>();
            Set<ObjectName> mbeans = server.queryNames(query, null);
            if (mbeans != null) {
                for (ObjectName objName : mbeans) {
                    data.add(new MBeanRateData(server,objName, "activeSessions"));

                }
            }
            return data;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
