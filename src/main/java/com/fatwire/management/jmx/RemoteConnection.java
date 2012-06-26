/**
 * 
 */
package com.fatwire.management.jmx;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fatwire.management.domain.Host;

public class RemoteConnection {

    private MBeanServerConnection server;

    private JMXConnector jmxc;

    private Host host;

    private static final Logger log = LoggerFactory
            .getLogger(RemoteConnection.class);

    public void close() throws IOException {
        this.jmxc.close();

    }

    public void connect(Host host, String login, String password)
            throws IOException {
        this.host = host;
        log
                .info("Connecting to " + host.getAddress() + ":"
                        + host.getJmxPort());

        // Create an RMI connector client and connect it to
        // the RMI connector server
        String urlPath = "/jndi/rmi://" + host.getAddress().getHostName() + ":"
                + host.getJmxPort() + "/jmxrmi";
        JMXServiceURL url = new JMXServiceURL("rmi", host.getAddress()
                .getHostName(), host.getJmxPort(), urlPath);
        log.info(url.toString());
        connect(url, login != null && password != null ? formatCredentials(
                login, password) : null);
    }

    public void connect(Host host) throws IOException {
        connect(host, null, null);
    }

    protected Map<String, ?> formatCredentials(final String login,
            final String password) {
        Map<String, String[]> env = null;
        String[] creds = new String[] { login, password };
        env = new HashMap<String, String[]>(1);
        env.put(JMXConnector.CREDENTIALS, creds);
        return env;
    }

    /**
     * Connect to a JMX agent of a given URL. 
     * @throws IOException 
     */
    private void connect(JMXServiceURL url, Map<String, ?> env)
            throws IOException {

        this.jmxc = JMXConnectorFactory.connect(url, env);
        this.server = jmxc.getMBeanServerConnection();

    }

    void printBeans() throws Exception {
        Writer w = new FileWriter("./mbeans.txt");
        Writer wn = new FileWriter("./mbean-names.txt");

        Set<ObjectName> names = new TreeSet<ObjectName>(server.queryNames(null,
                null));
        Set<String> types = new TreeSet<String>();
        Set<String> goodTypes = new TreeSet<String>();

        goodTypes.add("boolean");
        goodTypes.add("int");
        goodTypes.add("double");
        goodTypes.add(BigDecimal.class.getName());
        goodTypes.add(BigInteger.class.getName());
        //goodTypes.add("java.lang.ClassLoader");
        //goodTypes.add("java.lang.Object");
        goodTypes.add("java.lang.String");
        goodTypes.add("javax.management.ObjectName");
        goodTypes.add(javax.management.openmbean.CompositeData.class.getName());
        goodTypes.add(javax.management.openmbean.TabularData.class.getName());
        //goodTypes.add("javax.naming.directory.DirContext");
        goodTypes.add("long");
        //class java.lang.Integer
        for (ObjectName name : names) {
            wn.write(name.toString());
            wn.write("\n");
            System.out.println(name.toString());
            w.write("ObjectName = " + name);
            try {
                MBeanInfo info = server.getMBeanInfo(name);
                w.write(" (" + info.getClassName() + ")\n");
                Map<String, String> l = new HashMap<String, String>();
                for (MBeanAttributeInfo attr : info.getAttributes()) {
                    //w.write("\t (" + attr.getName() + ")\n");
                    try {
                        if (attr.isReadable()) {
                            if (attr.getType() != null) {
                                types.add(attr.getType());
                                //w.write("\t type=" + attr.getType() + "\n");
                                if (goodTypes.contains(attr.getType())) {
                                    l.put(attr.getName(), attr.getType());
                                } else if (attr.getType().startsWith("[L")
                                        && attr.getType().endsWith(";")) {
                                    //array
                                    String x = attr.getType().substring(2,
                                            attr.getType().length() - 3);
                                    if (goodTypes.contains(x)) {
                                        l.put(attr.getName(), attr.getType());
                                    }
                                } else {
                                    w.write("\t#" + attr.getName() + "("
                                            + attr.getType() + ")\n");
                                }
                            } else {
                                w.write("\t(Null Type) " + attr.getName()
                                        + ")\n");
                            }
                        } else {
                            w.write("\t(Not Readable) " + attr.getName() + "("
                                    + attr.getType() + ")\n");
                        }
                    } catch (UnsupportedOperationException e) {
                        log.error(e.getMessage(), e);
                    } catch (RuntimeMBeanException e) {
                        log.error(e.getMessage(), e);
                    } catch (javax.management.RuntimeOperationsException e) {
                        log.error(e.getMessage(), e);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                }
                List<Attribute> al = server.getAttributes(name,
                        l.keySet().toArray(new String[0])).asList();

                //w.write("\t size: " + al.size() + "\n");
                for (Attribute a : al) {
                    w.write("\t" + a.getName() + " (" + l.get(a.getName())
                            + ") ");
                    Object o = a.getValue();

                    if (o != null && o.getClass().isArray()) {
                        for (Object ao : (Object[]) o) {
                            w.write("\t\t" + ao + "\n");
                        }

                    } else if (CompositeData.class.getName().equals(
                            l.get(a.getName()))) {
                        CompositeData d = (CompositeData) o;
                        if (d != null && d.getCompositeType() != null) {
                            w.write("\n");
                            for (String s : d.getCompositeType().keySet()) {
                                w.write("\t\t" + s + "=" + d.get(s) + "\n");
                            }
                        }
                    } else {
                        w.write(o + "\n");

                    }

                }

            } catch (java.rmi.UnmarshalException e) {
                log.error(e.getMessage(), e);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        w.close();
        wn.close();
    }

    public MBeanServerConnection getServer() {
        return this.server;
    }

    public static void main(String[] args) throws UnknownHostException {
        if (!(args.length == 1 || args.length == 3))
            throw new IllegalArgumentException(
                    RemoteConnection.class.getName()
                            + " should be started with one argument hostname:port, followed by username and password if needed");
        String[] a = args[0].split(":");
        if (a.length != 2)
            throw new IllegalArgumentException(
                    RemoteConnection.class.getName()
                            + " should be started with one argument in the form of 'hostname:port'");
        RemoteConnection rc = new RemoteConnection();
        Host host = new Host();
        host.setAddress(InetAddress.getByName(a[0]));
        host.setJmxPort(Integer.parseInt(a[1]));

        try {
            if (args.length == 1) {
                rc.connect(host);
            } else {
                rc.connect(host, args[1], args[2]);
            }
            rc.printBeans();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Host getHost() {
        return host;
    }
}