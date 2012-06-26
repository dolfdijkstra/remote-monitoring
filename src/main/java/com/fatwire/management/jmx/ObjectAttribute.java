package com.fatwire.management.jmx;

import javax.management.ObjectName;



public class ObjectAttribute {
    public enum Type{COUNTER,GAUGE,DERIVE, ABSOLUTE };
    private final ObjectName objName;

    private final String attribute;
    
    private final String datasourceName;

    private final Type type;

    private final Class<?> valueClass;

    public ObjectAttribute(ObjectName objName, String attribute, Type type,
            Class<?> class1,final String datasourceName) {
        this.objName = objName;
        this.attribute = attribute;
        this.type = type;
        this.valueClass = class1;
        this.datasourceName=datasourceName;

    }

    public ObjectName getObjName() {

        return objName;
    }

    public String getAttribute() {
        return attribute;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the valueClass
     */
    public Class<?> getValueClass() {
        return valueClass;
    }

    public String getDatasourceName() {
        return datasourceName;
    }

}
