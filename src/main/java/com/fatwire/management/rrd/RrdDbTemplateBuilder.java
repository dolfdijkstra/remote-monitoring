package com.fatwire.management.rrd;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fatwire.management.JMXMetric;
import com.fatwire.management.jmx.ObjectAttribute;
import com.fatwire.management.jmx.ObjectAttribute.Type;

public class RrdDbTemplateBuilder {

    public Document build(JMXMetric metric) throws ParserConfigurationException {
        DocumentBuilder d = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = d.newDocument();
        Element root = doc.createElement("rrd_def");
        doc.appendChild(root);

        Element path = doc.createElement("path");
        path.appendChild(doc.createTextNode("${rrdfile}"));
        root.appendChild(path);
        Element start = doc.createElement("start");
        start.appendChild(doc.createTextNode("${start}"));
        root.appendChild(start);
        Element step = doc.createElement("step");
        step.appendChild(doc.createTextNode("${step}"));
        root.appendChild(step);
        for (ObjectAttribute oa : metric.getObjectAttributes()) {
            Element datasource = doc.createElement("datasource");
            Element name = doc.createElement("name");
            name.appendChild(doc.createTextNode(oa.getDatasourceName()));

            Element type = doc.createElement("type");
            type.appendChild(doc.createTextNode(toDsType(oa.getType()).name()));
            Element heartbeat = doc.createElement("heartbeat");
            heartbeat.appendChild(doc.createTextNode("10"));
            Element min = doc.createElement("min");
            min.appendChild(doc.createTextNode("0"));
            Element max = doc.createElement("max");
            max.appendChild(doc.createTextNode("U"));

            datasource.appendChild(name);
            datasource.appendChild(type);
            datasource.appendChild(heartbeat);
            datasource.appendChild(min);
            datasource.appendChild(max);

            root.appendChild(datasource);

        }
        ConsolFun[] fun = new ConsolFun[] { ConsolFun.AVERAGE, ConsolFun.MIN,
                ConsolFun.MAX, ConsolFun.LAST };
        for (ConsolFun f : fun) {
            addArchive(doc, root, f, 1, 1440);
            addArchive(doc, root, f, 3, 2880);
            addArchive(doc, root, f, 6, 2880);
            addArchive(doc, root, f, 42, 2880);
            addArchive(doc, root, f, 180, 2880);
        }
        return doc;
    }

    private void addArchive(Document doc, Element root, ConsolFun function,
            int steps, int rows) {
        Element archive = doc.createElement("archive");
        Element cf = doc.createElement("cf");
        cf.appendChild(doc.createTextNode(function.name()));
        archive.appendChild(cf);
        Element xff = doc.createElement("xff");
        xff.appendChild(doc.createTextNode("0.5"));
        archive.appendChild(xff);

        Element esteps = doc.createElement("steps");
        esteps.appendChild(doc.createTextNode(Integer.toString(steps)));
        archive.appendChild(esteps);

        Element erows = doc.createElement("rows");
        erows.appendChild(doc.createTextNode(Integer.toString(rows)));
        archive.appendChild(erows);

        root.appendChild(archive);
    }

    DsType toDsType(Type type) {
        //"COUNTER", "GAUGE", "DERIVE" or "ABSOLUTE"
        switch (type) {
        case ABSOLUTE:
            return DsType.ABSOLUTE;
        case COUNTER:
            return DsType.COUNTER;
        case DERIVE:
            return DsType.DERIVE;
        case GAUGE:
            return DsType.GAUGE;

        }
        throw new IllegalArgumentException(
                "Should not happen, all types are known:" + type);
    }

}
