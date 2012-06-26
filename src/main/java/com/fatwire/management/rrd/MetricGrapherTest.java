package com.fatwire.management.rrd;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.rrd4j.core.Util;

public class MetricGrapherTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGraphFileFileLongLongURLString()
            throws MalformedURLException {

        MetricGrapher mg = new MetricGrapher(new File("."));
        System.out.println(Util.getTimestamp() - (60 * 60 * (8 * 24 + 6)));
        System.out.println(Util.getTimestamp() - (60 * 60 * (8 * 24 + 4)));
        System.out.println(new Date(1246199622L *1000));
        System.out.println(new Date(1246206822L *1000));
        Calendar cal = Calendar.getInstance();
        cal.set(2009,5,28,17,20);
        long start = cal.getTimeInMillis()/1000;
        cal.set(2009,5,28,17,40);
        long end = cal.getTimeInMillis()/1000;
        mg
                .graph(
                        new File(
                                "./rrd/10.133.40.37_8087/Catalina/Servlet-ContentServer-__localhost_cs.rrd"),
                        new File(
                                "./test/Catalina/Servlet-ContentServer-__localhost_cs.png"),
                        start,end,
                        new File("./conf/template/graph/TomcatServlet.xml")
                                .toURI().toURL(), "Testing 123");
    }
}
