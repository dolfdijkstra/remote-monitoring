package com.fatwire.management.jmx;

import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public interface ModelDataFactory<T> {
    
    List<ModelData<T>> create(MBeanServerConnection server,ObjectName objName);

}
