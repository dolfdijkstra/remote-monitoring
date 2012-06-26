package com.fatwire.management.jmx.factory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fatwire.management.JMXMetric;
import com.fatwire.management.jmx.AttributeDescriptor;
import com.fatwire.management.jmx.ObjectAttribute;
import com.fatwire.management.jmx.RemoteConnection;
import com.fatwire.management.rrd.Rrd4jUtil;

public abstract class AbstractMetricFactory implements JMXMetricFactory {
    protected ObjectNameFilter filter;

    private static final Logger log = LoggerFactory
            .getLogger(AbstractMetricFactory.class);

    protected final Rrd4jUtil rrd4jUtil = new Rrd4jUtil();

    protected Set<ObjectName> foundNames = new HashSet<ObjectName>();

    public AbstractMetricFactory() {
        super();
    }

    /* (non-Javadoc)
     * @see com.fatwire.management.jmx.factory.JMXMetricFactory#create(javax.management.MBeanServerConnection, javax.management.ObjectName)
     */
    public final List<JMXMetric> create(final RemoteConnection rc) {
        Map<String, AttributeDescriptor> def = createDef();
        MBeanServerConnection server = rc.getServer();
        try {
            List<JMXMetric> data = new LinkedList<JMXMetric>();
            Set<ObjectName> mbeans = query(server);
            if (mbeans != null) {
                for (ObjectName objName : mbeans) {
                    if (!foundNames.contains(objName)) {
                        foundNames.add(objName);
                        try {
                            MBeanInfo info = server.getMBeanInfo(objName);
                            JMXMetric m = new JMXMetric(objName, rc.getHost());
                            String desc = getDesciption(objName);
                            log.debug("found descrption {} from {}", desc,
                                    objName);
                            m.setDescription(desc);
                            m.setDbTemplateUrl(locateDbTemplate(objName));
                            m
                                    .setGraphTemplateUrls(locateGraphTemplate(objName));
                            for (MBeanAttributeInfo attr : info.getAttributes()) {
                                if (def.containsKey(attr.getName())) {
                                    AttributeDescriptor descriptor = def
                                            .get(attr.getName());
                                    m.add(new ObjectAttribute(objName, attr
                                            .getName(), descriptor.getType(),
                                            classForName(attr.getType()),
                                            descriptor.getDatasoureName()));
                                }
                            }
                            data.add(m);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                }
            }
            return data;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private URL locateDbTemplate(ObjectName objName) {
        String shortName = getShortName();
        return this.rrd4jUtil.locateTemplate("conf/template/db/" + shortName
                + ".xml");

    }

    protected String getShortName() {
        String shortName = getClass().getName().substring(
                getClass().getPackage().getName().length() + 1);
        return shortName;
    }

    protected URL[] locateGraphTemplate(ObjectName objName) {
        String shortName = getShortName();
        File d = new File("conf/template/graph/");
        final Pattern p = Pattern.compile(shortName + ".*\\.xml");
        final List<URL> files = new ArrayList<URL>();
        d.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile()
                        && p.matcher(pathname.getName()).matches()) {
                    try {
                        files.add(pathname.toURI().toURL());
                    } catch (MalformedURLException e) {
                        log.error(e.getMessage() + " on " + pathname, e);
                    }
                    return true;
                }
                return false;
            }

        });

        return files.toArray(new URL[0]);

    }

    protected Set<ObjectName> query(final MBeanServerConnection server)
            throws IOException, MalformedObjectNameException {
        ObjectNameFilter filter = getFilter();
        Set<ObjectName> mbeans = server.queryNames(getQuery(), null);

        for (Iterator<ObjectName> i = mbeans.iterator(); i.hasNext();) {
            ObjectName n = i.next();
            if (!filter.filter(n)) {
                i.remove();
            }
        }
        return mbeans;
    }

    public ObjectNameFilter getFilter() {
        return filter != null ? filter : new ObjectNameFilter() {

            @Override
            public boolean filter(ObjectName mame) {

                return true;
            }

        };
    }

    protected abstract String getDesciption(ObjectName objName);

    private final Class<?>[] baseClasses = new Class[] { boolean.class,
            byte.class, char.class, double.class, float.class, int.class,
            long.class, short.class, Boolean.class, Byte.class,
            Character.class, Double.class, Float.class, Integer.class,
            Long.class, Short.class, String.class };

    private Pattern p = Pattern.compile("[\\?\\[\\]/\\\\=\\+<>:;\",\\*|]");

    Class<?> classForName(String name) throws ClassNotFoundException {
        if (name.startsWith("[L") && name.endsWith(";")) {
            //an array, ends with semi-colon
            System.out.println("array found:" + name);
            //javax.management.ObjectName;

            return Array.newInstance(
                    classForName(name.substring(2, name.length() - 1)), 0)
                    .getClass();
        }
        for (Class<?> c : baseClasses) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return Class.forName(name);
    }

    public abstract Map<String, AttributeDescriptor> createDef();

    public abstract ObjectName getQuery() throws MalformedObjectNameException,
            NullPointerException;

    /**
     * @param filter the filter to set
     */
    public void setFilter(ObjectNameFilter filter) {
        this.filter = filter;
    }

    protected String safeString(String... parts) {
        StringBuilder b = new StringBuilder();
        for (String s : parts) {
            if (b.length() > 0) {
                b.append('-');
            }
            String name = s;
            try {
                if (s != null) {
                    name = ObjectName.unquote(s);
                }
            } catch (Exception e) {
                //ignore
            }
            b.append(name != null ? p.matcher(name).replaceAll(

            "_") : "");

        }
        return b.toString();
    }
}