package com.fatwire.management.snmp.factory;

import java.util.LinkedList;
import java.util.List;

import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

import com.fatwire.management.SnmpMetric;
import com.fatwire.management.jmx.ObjectAttribute.Type;
import com.fatwire.management.snmp.SnmpConnection;
import com.fatwire.management.snmp.SnmpObjectAttribute;

public class LinuxLoadAveragesMetricFactory {

    class AttributeDef {
        OID oid;

        String name;

        Type type;

        public AttributeDef(OID oid2, String string, Type type) {
            this.oid = oid2;
            this.name = string;
            this.type = type;
        }

    }

    public List<SnmpMetric> create(final SnmpConnection con) {

        try {
            List<SnmpMetric> data = new LinkedList<SnmpMetric>();
            PDU pdu = new PDU();
            OID avg1 = new OID("1.3.6.1.4.1.2021.10.1.3.1");
            OID avg5 = new OID("1.3.6.1.4.1.2021.10.1.3.2");
            OID avg15 = new OID("1.3.6.1.4.1.2021.10.1.3.3");
            pdu.add(new VariableBinding(avg1)); //Load-Avg-1
            pdu.add(new VariableBinding(avg5));//Load-Avg-5
            pdu.add(new VariableBinding(avg15));//Load-Avg-15
            pdu.setType(PDU.GET);

            ResponseEvent event = con.send(pdu);
            if (event.getResponse() != null) {
                PDU response = event.getResponse();
                SnmpMetric m = new SnmpMetric(avg1.predecessor());
                for (VariableBinding binding : response.toArray()) {
                    for (AttributeDef def : defs()) {
                        if (def.oid.equals(binding.getOid())) {
                            m.add(new SnmpObjectAttribute(binding.getOid(),
                                    def.name, def.type, binding.getSyntax()));

                        }
                    }

                    data.add(m);

                }
            }
            return data;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    List<AttributeDef> defs() {
        List<AttributeDef> x = new LinkedList<AttributeDef>();
        x.add(new AttributeDef(new OID("1.3.6.1.4.1.2021.10.1.3.1"),
                "Load-Avg-1", Type.GAUGE));
        x.add(new AttributeDef(new OID("1.3.6.1.4.1.2021.10.1.3.2"),
                "Load-Avg-5", Type.GAUGE));
        x.add(new AttributeDef(new OID("1.3.6.1.4.1.2021.10.1.3.3"),
                "Load-Avg-15", Type.GAUGE));

        return x;
    }
}
