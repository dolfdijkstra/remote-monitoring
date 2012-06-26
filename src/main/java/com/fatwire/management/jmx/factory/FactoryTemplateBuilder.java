package com.fatwire.management.jmx.factory;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fatwire.management.jmx.RemoteConnection;

public class FactoryTemplateBuilder {

   public void build(RemoteConnection rc) throws Exception {
    }

    public Document build(ObjectName name, MBeanServerConnection server)
            throws Exception {
        MBeanInfo info = server.getMBeanInfo(name);

        DocumentBuilder d = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = d.newDocument();
        Element root = doc.createElement("factory");
        //root.setAttribute("class", factory.getClass().getName());

        doc.appendChild(root);

        Element query = doc.createElement("query");
        query.appendChild(doc.createTextNode(name.toString() + ",*"));
        root.appendChild(query);
        Element attr = doc.createElement("attributes");
        root.appendChild(attr);
        int i = 0;
        for (MBeanAttributeInfo e : info.getAttributes()) {
            Element a = doc.createElement("attribute");
            String dsn = e.getName();
            a.setAttribute("name", e.getName());
            a.setAttribute("type", e.getType());
            if (dsn.length() > 20) {
                dsn = "datasource-" + (i++);
                a.setAttribute("ds-name", dsn);
            }

            attr.appendChild(a);
        }
        Element desc = doc.createElement("description-keys");
        desc.setAttribute("prefix", "MyName");
        StringBuilder textContent = new StringBuilder();
        for (String s : name.getKeyPropertyList().keySet()) {
            if (textContent.length() > 0) {
                textContent.append(',');
            }
            textContent.append(s);
        }
        desc.setTextContent(textContent.toString());
        doc.appendChild(desc);
        return doc;

    }
}
