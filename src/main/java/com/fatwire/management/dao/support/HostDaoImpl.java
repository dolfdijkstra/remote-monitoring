package com.fatwire.management.dao.support;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fatwire.management.dao.HostDao;
import com.fatwire.management.domain.Host;

public class HostDaoImpl implements HostDao {
    private final Set<Host> hosts = new HashSet<Host>();

    @Override
    public synchronized void addHost(Host host) {
        hosts.add(host);

    }

    @Override
    public synchronized void deleteHost(Host host) {
        hosts.remove(host);

    }

    @Override
    public synchronized Collection<Host> findHosts() {
        return Collections.unmodifiableCollection(hosts);
    }

    @Override
    public Host findHost(String address, int port) {
        InetAddress a;
        try {
            a = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        for (Host host : findHosts()) {
            if (port == host.getJmxPort() && host.getAddress().equals(a)) {
                return host;
            }
        }
        return null;

    }

}
