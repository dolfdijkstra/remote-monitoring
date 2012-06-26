package com.fatwire.management.rrd;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import javax.swing.JFrame;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

public class SampleRrd {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        RrdDef rrdDef = new RrdDef("./all.rrd");
        rrdDef.setStep(1);
        //rrdDef.setStartTime(978300900L);
        long start = Util.getTimestamp(new Date(
                System.currentTimeMillis() - 300 * 1000));

        rrdDef.setStartTime(start);

        rrdDef.addDatasource("a", DsType.COUNTER, 30, Double.NaN, Double.NaN);
        //rrdDef.addDatasource("b", DsType.GAUGE, 600, Double.NaN, Double.NaN);
        //rrdDef.addDatasource("c", DsType.DERIVE, 600, Double.NaN, Double.NaN);
        //rrdDef.addDatasource("d", DsType.ABSOLUTE, 600, Double.NaN, Double.NaN);
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 3, 300);
        //rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 10, 30);
        //RrdMemoryBackendFactory f = new RrdMemoryBackendFactory();
        RrdDb rrdDb = new RrdDb(rrdDef);
        upDateDb(start, "./all.rrd");
        RrdGraphDef graphDef = new RrdGraphDef();
        graphDef.setImageFormat("GIF");
        //rrdDb.
        graphDef.setTimeSpan(start, start + 300);
        graphDef.datasource("linea", "./all.rrd", "a", ConsolFun.AVERAGE);
        //graphDef.datasource("lineb", "./all.rrd", "b", ConsolFun.AVERAGE);
        //graphDef.datasource("linec", "./all.rrd", "c", ConsolFun.AVERAGE);
        //graphDef.datasource("lined", "./all.rrd", "d", ConsolFun.AVERAGE);
        graphDef.line("linea", Color.RED, "Line A", 1);
        //graphDef.line("lineb", Color.GREEN, "Line B", 3);
        //graphDef.line("linec", Color.BLUE, "Line C", 3);
        //graphDef.line("lined", Color.BLACK, "Line D", 3);
        //graphDef.setFilename("./all1.gif");
        //graphDef.setFilename("-");
        graphDef.setWidth(800);
        graphDef.setHeight(400);
        final RrdGraph graph = new RrdGraph(graphDef);
        /*
        final BufferedImage bim = new BufferedImage(400, 400,
                BufferedImage.TYPE_INT_RGB);
        graph.render(bim.getGraphics());
        */
        final JFrame frame = new JFrame("RRD image");

        frame.addWindowListener(new WindowAdapter() {

            /* (non-Javadoc)
             * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
             */
            @Override
            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
                frame.dispose();
                System.exit(0);
            }

        });
        Panel panel = new Panel() {
            public void paint(Graphics g) {
                //g.drawImage(bim, 0, 0, null);
                graph.render(g);
            }
        };

        frame.getContentPane().add(panel);
        frame.setSize(800, 400);
        //frame.pack();
        frame.setVisible(true);

        //org.rrd4j.inspector.RrdInspector.main(new String[] { "./all.rrd" });
    }

    private static void upDateDb(long start, String file) throws IOException {
        Random r = new Random();
        double prev = 150d;
        for (int i = 1; i < 300; i++) {
            //System.out.println(i);
            RrdDb rrdDb = new RrdDb(file);
            Sample sample = rrdDb.createSample();

            sample.setTime(start + i);
            prev += r.nextInt(500);
            System.out.println(prev);
            sample.setValue(0, prev);
            sample.update();
            rrdDb.close();

        }
//        System.out.println(rrdDb.getDatasource(0).getMinValue());
//        System.out.println(rrdDb.getDatasource(0).getMaxValue());
        /*
        sample.setAndUpdate("978301200:300:1:600:300");
        sample.setAndUpdate("978301500:600:3:1200:600");
        sample.setAndUpdate("978301800:900:5:1800:900");
        sample.setAndUpdate("978302100:1200:3:2400:1200");
        sample.setAndUpdate("978302400:1500:1:2400:1500");
        sample.setAndUpdate("978302700:1800:2:1800:1800");
        sample.setAndUpdate("978303000:2100:4:0:2100");
        sample.setAndUpdate("978303300:2400:6:600:2400");
        sample.setAndUpdate("978303600:2700:4:600:2700");
        sample.setAndUpdate("978303900:3000:2:1200:3000");
        */
    }

}
