package com.fatwire.management.snmp;

import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpConnection {
    private Snmp snmp;

    private CommunityTarget target;

    public void connect(String address) throws IOException {
        Address targetAddress = GenericAddress.parse(address);
        CommunityTarget target;
        target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);
        TransportMapping transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);

        snmp.listen();

    }

    public ResponseEvent send(PDU pdu) throws IOException {
        return snmp.send(pdu, target);
    }

    public void send(PDU pdu, ResponseListener listener) throws IOException {
        snmp.send(pdu, target,null,listener);
    }

    public void close() throws IOException {
        snmp.close();
    }

    /**
     * @return the snmp
     */
    public Snmp getSnmp() {
        return snmp;
    }

    /**
     * @return the target
     */
    public CommunityTarget getTarget() {
        return target;
    }

}
