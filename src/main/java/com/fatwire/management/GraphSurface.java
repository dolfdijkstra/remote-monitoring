/**
 * 
 */
package com.fatwire.management;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.swing.JPanel;

import com.fatwire.management.MemoryMonitor.Model;

public class GraphSurface extends JPanel implements Runnable {

    /**
     * 
     */
    private static final long serialVersionUID = -3480494204201833181L;

    public Thread thread;

    public long sleepAmount = 1000;

    public int usageHistCount = 20000;

    private int w, h;

    private BufferedImage bimg;

    private Graphics2D big;

    private final Font font = new Font("Times New Roman", Font.PLAIN, 11);

    private int ascent, descent;

    private final Rectangle graphOutlineRect = new Rectangle();

    private final Rectangle2D mfRect = new Rectangle2D.Float();

    private final Rectangle2D muRect = new Rectangle2D.Float();

    private final Line2D graphLine = new Line2D.Float();

    private final Color graphColor = new Color(46, 139, 87);

    private final Color memoryFreeColor = new Color(0, 100, 0);

    java.util.LinkedList<ModelGraph> graphs = new LinkedList<ModelGraph>();

    public GraphSurface() {
        setBackground(Color.black);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (thread == null) {
                    start();
                } else {
                    stop();
                }
            }
        });

    }

    public void addModel(Model m) {
        graphs.add(new ModelGraph(m));
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 800);
    }

    @Override
    public void paint(final Graphics g) {
        final Dimension d = getSize();
        if (d.width != w || d.height != h) {
            w = d.width;
            h = d.height;
            //System.out.println("resize w: " + w + ", h" + h);
            bimg = (BufferedImage) createImage(w, h);
            big = bimg.createGraphics();
            big.setFont(font);
            final FontMetrics fm = big.getFontMetrics(font);
            ascent = fm.getAscent();
            descent = fm.getDescent();
        }

        if (big == null) {
            return;
        }

        big.setBackground(getBackground());
        big.clearRect(0, 0, w, h);

        int h1 = h / ((graphs.size() + graphs.size() % 2) / 2);
        int w1 = w / 2;

        int k = 0; // index of memory pool.
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < (graphs.size() + graphs.size() % 2) / 2; j++) {
                graphs.get(k).plotMemoryUsage(w1 * i, h1 * j, w1, h1);
                if (++k >= graphs.size()) {
                    i = 3;
                    j = (graphs.size() + graphs.size() % 2) / 2;
                    break;
                }
            }
        }
        g.drawImage(bimg, 0, 0, this);
    }

    /**
     * Visual presentation of a model
     * 
     * @author Dolf.Dijkstra
     * @since Jun 7, 2009
     */
    class ModelGraph {

        final Model model;

        private int columnInc;

        public ModelGraph(final Model mp) {
            this.model = mp;
        }

        public void plotMemoryUsage(final int x1, final int y1, final int x2,
                final int y2) {
            final float usedMemory = model.getUsed();
            final float totalMemory = model.getMax();

            // .. Draw allocated and used strings ..
            big.setColor(Color.green);

            // Print Max memory allocated for this memory pool.
            big.drawString(String.valueOf((int) totalMemory / 1024) + "K Max ",
                    x1 + 4.0f, (float) y1 + ascent + 0.5f);
            big.setColor(Color.yellow);

            // Print the memory pool name.
            big.drawString(model.getName(), x1 + x2 / 2, (float) y1 + ascent
                    + 0.5f);

            // Print the memory used by this memory pool.
            String usedStr = String.valueOf((int) usedMemory / 1024) + "K used";
            big.setColor(Color.green);
            big.drawString(usedStr, x1 + 4, y1 + y2 - descent);

            // Calculate remaining size
            int blockCount = 20;

            final float fontHeight = ascent + descent;
            float remainingHeight = (y2 - fontHeight * 2 - 0.5f);
            final float blockHeight = remainingHeight / blockCount;
            final float blockWidth = 20.0f;
            float remainingWidth = (x2 - blockWidth - blockCount);

            final int MemUsage = (int) ((totalMemory - usedMemory)
                    / totalMemory * blockCount);

            // .. Memory Free ..
            big.setColor(memoryFreeColor);
            int i = 0;
            for (; i < MemUsage; i++) {
                mfRect.setRect(x1 + 5, y1 + fontHeight + i * blockHeight,
                        blockWidth, blockHeight - 1);
                big.fill(mfRect);
            }

            // .. Memory Used ..
            big.setColor(Color.green);
            for (; i < blockCount; i++) {
                muRect.setRect(x1 + 5, y1 + fontHeight + i * blockHeight,
                        blockWidth, blockHeight - 1);
                big.fill(muRect);
            }

            // .. Draw History Graph ..
            if (remainingWidth <= 30) {
                remainingWidth = 30;
            }
            if (remainingHeight <= fontHeight) {
                remainingHeight = fontHeight;
            }
            big.setColor(graphColor);
            final int graphX = x1 + 30;
            final int graphY = y1 + (int) fontHeight;
            final int graphW = (int) remainingWidth;
            final int graphH = (int) remainingHeight;

            drawGrid(graphX, graphY, graphW, graphH);

            big.setColor(Color.YELLOW);

            int w1; // width of memory usage history.
            if (model.getNumberOfHistoryPoints() > graphW) {
                w1 = graphW;
            } else {
                w1 = model.getNumberOfHistoryPoints();
            }
            float[] usedMem = model.getUsedMem();
            for (int j = graphX + graphW - w1, k = model
                    .getNumberOfHistoryPoints()
                    - w1; k < model.getNumberOfHistoryPoints(); k++, j++) {
                if (k != 0) {
                    if (usedMem[k] != usedMem[k - 1]) {
                        final int h1 = (int) (graphY + graphH
                                * (totalMemory - usedMem[k - 1]) / totalMemory);
                        final int h2 = (int) (graphY + graphH
                                * (totalMemory - usedMem[k]) / totalMemory);
                        big.drawLine(j - 1, h1, j, h2);
                    } else {
                        final int h1 = (int) (graphY + graphH
                                * (totalMemory - usedMem[k]) / totalMemory);
                        big.fillRect(j, h1, 1, 1);
                    }
                }
            }
        }

        private void drawGrid(final int graphX, final int graphY,
                final int graphW, final int graphH) {
            graphOutlineRect.setRect(graphX, graphY, graphW, graphH);
            big.draw(graphOutlineRect);
            final int graphRow = graphH / 10;

            // .. Draw row ..
            for (int j = graphY; j <= graphH + graphY; j += graphRow) {
                graphLine.setLine(graphX, j, graphX + graphW, j);
                big.draw(graphLine);
            }

            // .. Draw animated column movement ..
            final int graphColumn = graphW / 10;

            if (columnInc == 0) {
                columnInc = graphColumn;
            }

            for (int j = graphX + columnInc; j < graphW + graphX; j += graphColumn) {
                graphLine.setLine(j, graphY, j, graphY + graphH);
                big.draw(graphLine);
            }
            --columnInc;
        }

    }

    public void start() {
        thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setName("MemoryMonitor");
        thread.start();
    }

    public synchronized void stop() {
        thread = null;
        notify();
    }

    public void run() {

        final Thread me = Thread.currentThread();

        while (thread == me && !isShowing() || getSize().width == 0) {
            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                return;
            }
        }

        while (thread == me && isShowing()) {
            repaint();
            try {
                Thread.sleep(sleepAmount);
            } catch (final InterruptedException e) {
                break;
            }
        }
        thread = null;
    }
}