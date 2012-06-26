package com.fatwire.management.jmx;

import com.fatwire.management.jmx.ObjectAttribute.Type;
import com.fatwire.management.rrd.RrdUtil;

public class AttributeDescriptor {
    private final String name;
    private final String datasoureName;

    private final Type type;

    /**
     * @param name
     * @param type
     * @param datasoureName
     */
    public AttributeDescriptor(String name, Type type, String datasoureName) {
        super();
        this.name = name;
        this.type = type;
        
        this.datasoureName = RrdUtil.truncateDsName(datasoureName);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the datasoureName
     */
    public String getDatasoureName() {
        return datasoureName;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

}
