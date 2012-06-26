package com.fatwire.management.jmx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;

import javax.management.ObjectName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.fatwire.management.JMXMetric;
import com.fatwire.management.domain.Host;
import com.fatwire.management.jmx.MetricDiscoverer.DiscovererListener;
import com.fatwire.management.jmx.factory.FactoryTemplateBuilder;
import com.fatwire.management.rrd.RrdDbTemplateBuilder;
import com.fatwire.management.rrd.RrdGraphTemplateBuilder;
import com.fatwire.management.util.FileUtil;

public class Discoverer {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws Exception {
        //        String host = "localhost";
        //        int port = 12345;
        String host = "radium.nl.fatwire.com";
        int port = 8087;

        File[] factories = new File("conf/template/factory/")
                .listFiles(new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".xml");
                    }
                });

        final RemoteConnection connection = new RemoteConnection();
        try {
            Host h = new Host();
            h.setAddress(InetAddress.getByName(host));
            h.setJmxPort(port);

            connection.connect(h);

            final Poller poller = new Poller();
            poller.setServer(connection);

            MetricDiscoverer discoverer = new MetricDiscoverer(factories, h);
            DiscovererListener listener = new DiscovererListener() {

                @Override
                public void onDiscoveryEnd(Host host) {

                }

                @Override
                public void onDiscoveryStart(Host host) {

                }

                @Override
                public void onMetricFoundEvent(MetricFoundEvent e) {
                    poller.addManagementDataReference(e.getMetric());

                }

            };
            discoverer.addListener(listener);
            discoverer.discover(connection);
            RrdDbTemplateBuilder builder = new RrdDbTemplateBuilder();
            RrdGraphTemplateBuilder gbuilder = new RrdGraphTemplateBuilder();

            for (JMXMetric metric : poller.getDataToPoll()) {
                System.out.println(metric.getObjName());

                Document doc = gbuilder.build(metric);
                File f = new File("conf/ftmpl/" + metric.getDescription()
                        + ".xml");
                write(doc, f);

            }
        } finally {
            connection.close();
        }
    }

    static void write(Document doc, File file) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        //identity
        file.getParentFile().mkdirs();
        FileOutputStream os = new FileOutputStream(file);
        try {
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(doc), new StreamResult(os));
        } finally {
            if (os != null)
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    }

    void foo(RemoteConnection rc) throws Exception {
        FactoryTemplateBuilder fbuilder = new FactoryTemplateBuilder();
        for (ObjectName name : rc.getServer().queryNames(null, null)) {
            Document doc = fbuilder.build(name, rc.getServer());
            TransformerFactory tf = TransformerFactory.newInstance();
            //identity
            File f = new File("conf/ftmpl/"
                    + FileUtil.filenameSafeString(name.toString()) + ".xml");
            f.getParentFile().mkdirs();
            FileOutputStream os = new FileOutputStream(f);
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(doc), new StreamResult(os));
            os.close();

        }

    }
}
