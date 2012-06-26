package com.fatwire.management.dao;

import java.util.Collection;

import com.fatwire.management.domain.Host;

public interface HostDao {

    Collection<Host> findHosts();
    
    Host findHost(String address, int port);

    void addHost(Host host);

    void deleteHost(Host host);
}
