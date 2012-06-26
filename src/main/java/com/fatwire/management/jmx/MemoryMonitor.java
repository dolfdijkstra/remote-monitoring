package com.fatwire.management.jmx;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.management.ObjectName;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.fatwire.management.domain.Host;

/**
 * Demo code which plots the memory usage by all memory pools. The memory usage
 * is sampled at some time interval using java.lang.management API. This demo
 * code is modified based java2d MemoryMonitor demo.
 */
public class MemoryMonitor extends JPanel {

    public Surface surf;

    private JPanel controls;

    private boolean doControls;

    private JTextField tf;

    // Get memory pools.
    private java.util.List<ModelData<Float>> data;/* = ManagementFactory.getMemoryPoolMXBeans();*/

    // Total number of memory pools.
    private int numPools;

    public MemoryMonitor(java.util.List<ModelData<Float>> data) {
        this.data = data;
        numPools = data.size();
        setLayout(new BorderLayout());
        setBorder(new TitledBorder(new EtchedBorder(), "Memory Monitor"));
        add(surf = new Surface());
        controls = new JPanel();
        //controls.setPreferredSize(new Dimension(135, 80));
        Font font = new Font("serif", Font.PLAIN, 10);
        JLabel label = new JLabel("Sample Rate");
        label.setFont(font);
        label.setForeground(Color.red);
        controls.add(label);
        tf = new JTextField("1000");
        tf.setPreferredSize(new Dimension(45, 20));
        controls.add(tf);
        controls.add(label = new JLabel("ms"));
        label.setFont(font);
        label.setForeground(Color.red);
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                removeAll();
                if ((doControls = !doControls)) {
                    surf.stop();
                    add(controls);
                } else {
                    try {
                        surf.sleepAmount = Long.parseLong(tf.getText().trim());
                    } catch (Exception ex) {
                    }
                    surf.start();
                    add(surf);
                }
                validate();
                repaint();
            }
        });
    }

    public class Surface extends JPanel implements Runnable {

        public Thread thread;

        public long sleepAmount = 1000;

        public int usageHistCount = 20000;

        private int w, h;

        private BufferedImage bimg;

        private Graphics2D big;

        private Font font = new Font("Times New Roman", Font.PLAIN, 11);

        private int columnInc;

        //private History<Float> history[];

        private int ascent, descent;

        private Rectangle graphOutlineRect = new Rectangle();

        private Rectangle2D mfRect = new Rectangle2D.Float();

        private Rectangle2D muRect = new Rectangle2D.Float();

        private Line2D graphLine = new Line2D.Float();

        private Color graphColor = new Color(46, 139, 87);

        private Color mfColor = new Color(0, 100, 0);

        private String usedStr;

        public Surface() {
            setBackground(Color.black);
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (thread == null)
                        start();
                    else
                        stop();
                }
            });
            //history = new History[numPools];

        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public Dimension getPreferredSize() {
            return new Dimension(600, 500);
        }

        public void paint(Graphics g) {

            if (big == null) {
                return;
            }

            big.setBackground(getBackground());
            big.clearRect(0, 0, w, h);
            int cols = numPools > 4 ? (numPools / 4) + 1 : 1;
            h = h / ((numPools + numPools % cols) / cols);
            w = w / cols;

            int k = 0; // index of memory pool.
            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < (numPools + numPools % cols) / cols; j++) {
                    plotMemoryUsage(w * i, h * j, w, h, k);
                    if (++k >= numPools) {
                        i = cols + 1;
                        j = (numPools + numPools % cols) / cols;
                        break;
                    }
                }
            }
            g.drawImage(bimg, 0, 0, this);
        }

        public void plotMemoryUsage(int x1, int y1, int width, int height,
                int npool) {
            //System.out.println(x1 + " " + y1 + " " + width + " " + height + " "
            //		+ npool);
            ModelData<Float> mp = data.get(npool);
            //mp.refreshData();
            float usedValue = mp.getValue();
            float maxValue = mp.getMax();
            int base = 1;
            // .. Draw allocated and used strings ..
            big.setColor(Color.green);

            // Print Max memory allocated for this memory pool.
            big.drawString(String.valueOf((int) maxValue / base) + " max ",
                    x1 + 4.0f, (float) y1 + ascent + 0.5f);
            big.setColor(Color.yellow);

            // Print the memory pool name.
            big.drawString(mp.getName(), x1 + width / 2, (float) y1 + ascent
                    + 0.5f);

            // Print the memory used by this memory pool.
            usedStr = String.valueOf((int) usedValue / base) + " used";
            big.setColor(Color.green);
            big.drawString(usedStr, x1 + 4, y1 + height - descent);

            // Calculate remaining size
            float ssH = ascent + descent;
            float remainingHeight = (float) (height - (ssH * 2) - 0.5f);
            float blockHeight = remainingHeight / 10;
            float blockWidth = 20.0f;
            float remainingWidth = (float) (width - blockWidth - 10);

            // .. Memory Free , print blocks
            big.setColor(mfColor);
            int MemUsage = (int) (((maxValue - usedValue) / maxValue) * 10);
            int i = 0;
            for (; i < MemUsage; i++) {
                mfRect.setRect(x1 + 5, (float) y1 + ssH + i * blockHeight,
                        blockWidth, (float) blockHeight - 1);
                big.fill(mfRect);
            }

            // .. Memory Used ..
            big.setColor(Color.blue);
            for (; i < 10; i++) {
                muRect.setRect(x1 + 5, (float) y1 + ssH + i * blockHeight,
                        blockWidth, (float) blockHeight - 1);
                big.fill(muRect);
            }

            // .. Draw History Graph ..
            if (remainingWidth <= 30)
                remainingWidth = (float) 30;
            if (remainingHeight <= ssH)
                remainingHeight = (float) ssH;
            big.setColor(graphColor);
            int graphX = x1 + 30;
            int graphY = y1 + (int) ssH;
            int graphW = (int) remainingWidth;
            int graphH = (int) remainingHeight;

            graphOutlineRect.setRect(graphX, graphY, graphW, graphH);
            big.draw(graphOutlineRect);

            int graphRow = graphH / 10;

            // .. Draw row ..
            for (int j = graphY; j <= graphH + graphY; j += graphRow) {
                graphLine.setLine(graphX, j, graphX + graphW, j);
                big.draw(graphLine);
            }

            // .. Draw animated column movement ..
            int graphColumn = graphW / 15;

            if (columnInc == 0) {
                columnInc = graphColumn;
            }

            for (int j = graphX + columnInc; j < graphW + graphX; j += graphColumn) {
                graphLine.setLine(j, graphY, j, graphY + graphH);
                big.draw(graphLine);
            }

            --columnInc;

            // Plot memory usage by this memory pool.
            //            if (history[npool] == null) {
            //                history[npool] = new History(usageHistCount);
            //            }

            // save memory usage history.
            //history[npool].add(usedValue);

            History<Float> history = mp.getHistory();
            big.setColor(Color.yellow);

            int w1; // width of memory usage history.
            if (history.getCount() > graphW) {
                w1 = graphW;
            } else {
                w1 = history.getCount();
            }

            for (int j = graphX + graphW - w1, k = history.getCount() - w1; k < history
                    .getCount(); k++, j++) {
                if (k != 0) {
                    if (history.get(k) != history.get(k - 1)) {
                        int h1 = (int) (graphY + graphH
                                * ((maxValue - history.get(k - 1)) / maxValue));
                        int h2 = (int) (graphY + graphH
                                * ((maxValue - history.get(k)) / maxValue));
                        big.drawLine(j - 1, h1, j, h2);
                    } else {
                        int h1 = (int) (graphY + graphH
                                * ((maxValue - history.get(k)) / maxValue));
                        big.fillRect(j, h1, 1, 1);
                    }
                }
            }
        }

        public void start() {
            thread = new Thread(this);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setName("MemoryMonitor");
            thread.start();
            Timer timer = new Timer();

            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                        for (ModelData<Float> mp : data) {
                            try {
                                mp.refreshData();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                }

            }, 0, 1000);
        }

        public synchronized void stop() {
            System.out.println("stopping");
            thread = null;
            notify();
        }

        public void run() {

            Thread me = Thread.currentThread();

            while (thread == me && !isShowing() || getSize().width == 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return;
                }
            }

            while (thread == me && isShowing()) {
                Dimension d = getSize();
                if (d.width != w || d.height != h) {
                    w = d.width;
                    h = d.height;
                    bimg = (BufferedImage) createImage(w, h);
                    big = bimg.createGraphics();
                    big.setFont(font);
                    FontMetrics fm = big.getFontMetrics(font);
                    ascent = (int) fm.getAscent();
                    descent = (int) fm.getDescent();
                }
                repaint();
                try {
                    Thread.sleep(sleepAmount);
                } catch (InterruptedException e) {
                    break;
                }
            }
            thread = null;
        }
    }

    public static void main(String args[]) throws Exception {
        final RemoteConnection connection = new RemoteConnection();
        Host host = new Host();
        host.setAddress(InetAddress.getByName("radium"));
        host.setJmxPort(8087);
        connection.connect(host);
        //connection.printBeans();
        //connection.close();
        //System.exit(0);

        ObjectName x = new ObjectName(
                "Catalina:j2eeType=Servlet,name=ContentServer,WebModule=//localhost/cs,J2EEApplication=none,J2EEServer=none");
        ObjectName n = new ObjectName("Catalina:j2eeType=Servlet,*");
        String[] a = new String[] {
                "Catalina:j2eeType=Servlet,name=BlobServer,WebModule=//localhost/cs,J2EEApplication=none,J2EEServer=none",
                "Catalina:j2eeType=Servlet,name=CacheServer,WebModule=//localhost/cs,J2EEApplication=none,J2EEServer=none",
                "Catalina:j2eeType=Servlet,name=CatalogManager,WebModule=//localhost/cs,J2EEApplication=none,J2EEServer=none",
                "Catalina:j2eeType=Servlet,name=ContentServer,WebModule=//localhost/cs,J2EEApplication=none,J2EEServer=none",
                "Catalina:j2eeType=Servlet,name=Resources,WebModule=//localhost/cs,J2EEApplication=none,J2EEServer=none",
                "Catalina:j2eeType=Servlet,name=Satellite,WebModule=//localhost/cs,J2EEApplication=none,J2EEServer=none" };
        TomcatServletModelDataFactory factory = new TomcatServletModelDataFactory();
        List<ModelData<Float>> data = new LinkedList<ModelData<Float>>();
        for (String s : a) {
            data.addAll(factory.create(connection.getServer(),
                    new ObjectName(s)));
        }
        data.addAll(new TomcatSessionModelDataFactory().create(connection
                .getServer(), new ObjectName(
                "Catalina:type=Manager,path=/cs,host=localhost")));
        final MemoryMonitor demo = new MemoryMonitor(data);
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                demo.surf.stop();
                try {
                    File f = new File(System.getProperty("java.io.tmpdir"),
                            "monitoring-" + System.currentTimeMillis() + ".png");
                    System.out.println(f);
                    ImageIO.write(demo.surf.bimg, "PNG", f);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                try {

                    connection.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
            }

            public void windowDeiconified(WindowEvent e) {
                demo.surf.start();
            }

            public void windowIconified(WindowEvent e) {
                demo.surf.stop();
            }
        };
        JFrame f = new JFrame("MemoryMonitor");
        f.addWindowListener(l);
        f.getContentPane().add("Center", demo);
        f.pack();

        f.setSize(new Dimension(600, 500));
        f.setLocationRelativeTo(null);

        //f.setExtendedState(JFrame.MAXIMIZED_BOTH);
        f.setVisible(true);
        demo.surf.start();
        //Thread thr = new Thread(new Memeater());
        //Thread thr = new Thread(new MemUser());
        //thr.start();
    }
}
