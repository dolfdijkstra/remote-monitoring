package com.fatwire.management.jmx.factory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fatwire.management.jmx.AttributeDescriptor;
import com.fatwire.management.jmx.ObjectAttribute.Type;
import com.fatwire.management.rrd.RrdUtil;

public class XmlMetricFactory extends AbstractMetricFactory {

    private File xmlFile;

    private Document doc;

    private String prefix = "";

    private String[] keys;

    public void setXmlFile(File file) {
        this.xmlFile = file;
    }

    void read() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder d;
        try {
            d = dbf.newDocumentBuilder();
            doc = d.parse(xmlFile);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (doc == null)
            throw new RuntimeException("Document " + xmlFile
                    + " could not be parsed.");

    }

    @Override
    public final Map<String, AttributeDescriptor> createDef() {
        if (doc == null)
            read();
        Map<String, AttributeDescriptor> map = new HashMap<String, AttributeDescriptor>();
        NodeList attributes = doc.getElementsByTagName("attribute");
        for (int i = 0; i < attributes.getLength(); i++) {
            Element a = (Element) attributes.item(i);

            String dsn = a.getAttribute("ds-name");
            String name = a.getAttribute("name");
            if (dsn == null || dsn.length() < 1) {
                dsn = RrdUtil.truncateDsName(name);
            }
            AttributeDescriptor d = new AttributeDescriptor(name, Type.valueOf(
                    Type.class, a.getAttribute("type").trim()), dsn);
            map.put(name, d);
        }
        return map;
    }

    @Override
    protected String getDesciption(ObjectName objName) {
        if (doc == null)
            read();
        
        String[] parts;
        if (keys == null) {
            NodeList descList = doc.getElementsByTagName("description-keys");
            if (descList != null && descList.getLength() == 1) {
                Element dk = (Element) descList.item(0);
                keys = dk.getTextContent().split(",");
                prefix = dk.getAttribute("prefix");
            } else {
                throw new IllegalStateException(
                        "description-keys tag not found or had mulitple entries in file "
                                + xmlFile);
                //if nothing found, return all
                //            parts = objName.getKeyPropertyList().keySet()
                //                    .toArray(new String[0]);
            }
        }
        parts = new String[keys.length];

        for (int i = 0; i < keys.length; i++) {
            parts[i] = objName.getKeyProperty(keys[i]);
        }

        return (prefix.length() == 0 ? "" : prefix + "-")
                + this.safeString(parts);

    }

    @Override
    public ObjectName getQuery() throws MalformedObjectNameException,
            NullPointerException {
        if (doc == null)
            read();
        //<query>com.fatwire.cs:type=RequestCounter</query>
        return ObjectName.getInstance(doc.getElementsByTagName("query").item(0)
                .getTextContent());
    }
    protected String getShortName() {
        String shortName = this.xmlFile.getName().substring(0,this.xmlFile.getName().lastIndexOf('.'));
        return shortName;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getName()+":" + this.xmlFile;
    }

}
