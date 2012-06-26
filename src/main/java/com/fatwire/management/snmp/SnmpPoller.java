package com.fatwire.management.snmp;

import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.VariableBinding;

public class SnmpPoller {

    Snmp snmp;

    CommunityTarget target;

    public void setConnection(SnmpConnection con) throws IOException {
        this.target = con.getTarget();
        this.snmp = con.getSnmp();

    }

    public void poll() {
        try {
            // creating PDU

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.2021.10.1.3.1"))); //Load-Avg-1
            pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.2021.10.1.3.2")));//Load-Avg-5
            pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.2021.10.1.3.3")));//Load-Avg-5
            pdu.setType(PDU.GET);

            // sending request
            ResponseListener listener = new ResponseListener() {
                public void onResponse(ResponseEvent event) {
                    // Always cancel async request when response has been received
                    // otherwise a memory leak is created! Not canceling a request
                    // immediately can be useful when sending a request to a broadcast
                    // address.
                    System.out.println("Received request PDU is: "
                            + event.getRequest());
                    ((Snmp) event.getSource()).cancel(event.getRequest(), this);
                    System.out.println("Received response PDU is: "
                            + event.getResponse());
                    for (VariableBinding vb : event.getResponse().toArray()) {

                        if (vb.getSyntax() != SMIConstants.SYNTAX_OBJECT_IDENTIFIER) {
                            System.out.println(vb.getOid() + "="
                                    + vb.getVariable().toString());

                        }

                    }

                }
            };

            snmp.send(pdu, target, null, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
