package com.fatwire.management.jmx;

import javax.management.ObjectName;

public class ManagementDataReference {

    private final ObjectName objName;
    private final String attribute;
    /**
     * @param objName
     * @param attribute
     */
    public ManagementDataReference(ObjectName objName, String attribute) {
        super();
        this.objName = objName;
        this.attribute = attribute;
    }
    /**
     * @return the objName
     */
    public ObjectName getObjName() {
        return objName;
    }
    /**
     * @return the attribute
     */
    public String getAttribute() {
        return attribute;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((attribute == null) ? 0 : attribute.hashCode());
        result = prime * result + ((objName == null) ? 0 : objName.hashCode());
        return result;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ManagementDataReference)) {
            return false;
        }
        ManagementDataReference other = (ManagementDataReference) obj;
        if (attribute == null) {
            if (other.attribute != null) {
                return false;
            }
        } else if (!attribute.equals(other.attribute)) {
            return false;
        }
        if (objName == null) {
            if (other.objName != null) {
                return false;
            }
        } else if (!objName.equals(other.objName)) {
            return false;
        }
        return true;
    }
    


}
