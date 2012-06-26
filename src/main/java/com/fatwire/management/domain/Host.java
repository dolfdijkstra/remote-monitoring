package com.fatwire.management.domain;

import java.net.InetAddress;

public class Host {

    private InetAddress address;
    private int jmxPort;
    /**
     * @return the address
     */
    public InetAddress getAddress() {
        return address;
    }
    /**
     * @param address the address to set
     */
    public void setAddress(InetAddress address) {
        this.address = address;
    }
    /**
     * @return the jmxPort
     */
    public int getJmxPort() {
        return jmxPort;
    }
    /**
     * @param jmxPort the jmxPort to set
     */
    public void setJmxPort(int jmxPort) {
        this.jmxPort = jmxPort;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + jmxPort;
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
        if (!(obj instanceof Host)) {
            return false;
        }
        Host other = (Host) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        if (jmxPort != other.jmxPort) {
            return false;
        }
        return true;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.address.getHostAddress() +":" + this.jmxPort;
    }
    
}
