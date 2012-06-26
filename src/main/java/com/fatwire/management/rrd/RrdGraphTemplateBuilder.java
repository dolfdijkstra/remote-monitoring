package com.fatwire.management.rrd;

import java.awt.Color;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.rrd4j.DsType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fatwire.management.JMXMetric;
import com.fatwire.management.jmx.ObjectAttribute;
import com.fatwire.management.jmx.ObjectAttribute.Type;

public class RrdGraphTemplateBuilder {
    private static Color[] colors = new Color[] { Color.RED, Color.BLUE, Color.GREEN,
            Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.PINK };

    class DocBuilder {
        final private Document doc;

        /**
         * @param doc
         */
        public DocBuilder(Document doc) {
            super();
            this.doc = doc;
        }

        Element createSimpleElement(String name, String text) {
            Element e = doc.createElement(name);
            e.appendChild(doc.createTextNode(text));
            return e;
        }

    }

    public Document build(JMXMetric metric) throws ParserConfigurationException {
        DocumentBuilder d = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = d.newDocument();
        Element root = doc.createElement("rrd_graph_def");
        doc.appendChild(root);
        DocBuilder db = new DocBuilder(doc);

        root.appendChild(db.createSimpleElement("filename", "${imagefile}"));
        Element span = doc.createElement("span");
        span.appendChild(db.createSimpleElement("start", "${intervalstart}"));
        span.appendChild(db.createSimpleElement("end", "${intervalend}"));
        root.appendChild(span);
        Element options = doc.createElement("options");
        options.appendChild(db.createSimpleElement("title", "${imagetitle}"));
        options.appendChild(db.createSimpleElement("width", "600"));
        options.appendChild(db.createSimpleElement("height", "200"));
        //options.appendChild(db.createSimpleElement("units_exponent", "0"));
        options.appendChild(db.createSimpleElement("rigid", "false"));
        options.appendChild(db.createSimpleElement("logarithmic",
                "${logarithmic}"));
        options.appendChild(db.createSimpleElement("min_value", "0"));
        options.appendChild(db.createSimpleElement("image_format", "png"));
        //options.appendChild(db.createSimpleElement("show_signature", "false"));

        root.appendChild(options);
        Element datasources = doc.createElement("datasources");
        root.appendChild(datasources);
        Element graph = doc.createElement("graph");
        root.appendChild(graph);

        int i = 0;
        for (ObjectAttribute v : metric.getObjectAttributes()) {

            Element def = doc.createElement("def");
            datasources.appendChild(def);
            def.appendChild(db.createSimpleElement("name", "ds" + i));
            def.appendChild(db.createSimpleElement("rrd", "${rrdfile}"));
            def.appendChild(db.createSimpleElement("source", v.getDatasourceName()));
            def.appendChild(db.createSimpleElement("cf", "AVERAGE"));
            Element line = doc.createElement("line");
            graph.appendChild(line);
            line.appendChild(db.createSimpleElement("datasource", "ds" + i));
            line.appendChild(db.createSimpleElement("color",
                    toHexColor(colors[i])));
            line
                    .appendChild(db.createSimpleElement("legend", v
                            .getAttribute()));
            line.appendChild(db.createSimpleElement("width", "2"));

            i++;

        }

        return doc;
    }

    String toHexColor(Color c) {
        return "#" + Integer.toHexString(c.getRGB() & 0x00ffffff);
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
