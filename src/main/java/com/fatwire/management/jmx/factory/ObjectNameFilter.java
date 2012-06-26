package com.fatwire.management.jmx.factory;

import javax.management.ObjectName;

public interface ObjectNameFilter {

    boolean filter(ObjectName mame);
    
}
