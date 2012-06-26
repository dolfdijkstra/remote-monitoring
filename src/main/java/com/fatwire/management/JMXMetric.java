package com.fatwire.management;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;

import com.fatwire.management.domain.Host;
import com.fatwire.management.jmx.ObjectAttribute;

public class JMXMetric {

    private String type;

    private final ObjectName oid;

    private long pollInterval;

    private String description;

    private URL[] graphTemplateUrl;

    private URL dbTemplateUrl;

    private List<ObjectAttribute> objectAttributes = new ArrayList<ObjectAttribute>();

    private Set<String> attributeMames = new HashSet<String>();

    private final Host host;

    /**
     * @param oid
     */
    public JMXMetric(ObjectName oid, final Host host) {
        super();
        this.oid = oid;
        this.host = host;

    }

    public List<ObjectAttribute> getObjectAttributes() {
        return objectAttributes;
    }

    public void add(ObjectAttribute objectAttribute) {
        objectAttributes.add(objectAttribute);
        attributeMames.add(objectAttribute.getAttribute());

    }

    /**
     * @return the oid
     */
    public ObjectName getOid() {
        return oid;
    }

    public String[] getAttributes() {
        return attributeMames.toArray(new String[0]);
    }

    public ObjectName getObjName() {
        return oid;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public URL[] getGraphTemplateUrls() {

        return graphTemplateUrl;

    }

    /**
     * @param templateUrl the templateUrl to set
     */
    public void setGraphTemplateUrls(URL[] templateUrl) {
        this.graphTemplateUrl = templateUrl;
    }

    public URL getDbTemplateUrl() {
        return dbTemplateUrl;
    }

    /**
     * @param dbTemplateUrl the dbTemplateUrl to set
     */
    public void setDbTemplateUrl(URL dbTemplateUrl) {
        this.dbTemplateUrl = dbTemplateUrl;
    }

    public ObjectAttribute getObjectAttribute(String name) {
        if (name == null)
            return null;
        for (ObjectAttribute oa : objectAttributes) {
            if (name.equals(oa.getAttribute()))
                return oa;
        }

        return null;
    }

    public Host getHost() {
        return host;
    }
}
