package com.fatwire.management;

/*
 * @(#)MemoryMonitor.java	1.2 04/07/27
 * 
 * Copyright (c) 2004 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright notice, 
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may 
 * be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL 
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST 
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY 
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, 
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

/*
 * @(#)MemoryMonitor.java	1.2 04/07/27
 */

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
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * Demo code which plots the memory usage by all memory pools. The memory usage
 * is sampled at some time interval using java.lang.management API. This demo
 * code is modified based java2d MemoryMonitor demo.
 */
public class GCMonitor extends JPanel {

	static JCheckBox dateStampCB = new JCheckBox("Output Date Stamp");
	public Surface surf;
	JPanel controls;
	boolean doControls;
	JTextField tf;
	// Get memory pools.

	static java.util.List<GarbageCollectorMXBean> mpools = ManagementFactory
			.getGarbageCollectorMXBeans();
	// Total number of memory pools.
	static int numPools = mpools.size();

	public GCMonitor() {
		setLayout(new BorderLayout());
		setBorder(new TitledBorder(new EtchedBorder(), "Memory Monitor"));
		add(surf = new Surface());
		controls = new JPanel();
		controls.setPreferredSize(new Dimension(135, 80));
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
		controls.add(dateStampCB);
		dateStampCB.setFont(font);
		for (GarbageCollectorMXBean mp : mpools){
			System.out.println("= "+mp.getName());
			for (String s: mp.getMemoryPoolNames()){
				System.out.println(s);
			}
		}
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
		private float usedMem[][];
		private int ptNum[];
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
			int i = 0;
			usedMem = new float[numPools][];
			ptNum = new int[numPools];
		}

		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		public Dimension getPreferredSize() {
			return new Dimension(135, 80);
		}

		public void paint(Graphics g) {

			if (big == null) {
				return;
			}

			big.setBackground(getBackground());
			big.clearRect(0, 0, w, h);

			h = h / ((numPools + numPools % 2) / 2);
			System.out.println("h="+h);
			w = w / 2;
			System.out.println("w="+w);
			int k = 0; // index of memory pool.
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < (numPools + numPools % 2) / 2; j++) {
					plotMemoryUsage(w * i, h * j, w, h, k);
					if (++k >= numPools) {
						i = 3;
						j = (numPools + numPools % 2) / 2;
						break;
					}
				}
			}
			g.drawImage(bimg, 0, 0, this);
		}

		public void plotMemoryUsage(int x1, int y1, int x2, int y2, int npool) {

			GarbageCollectorMXBean mp = mpools.get(npool);
			
			long collectionCount = mp.getCollectionCount();
			long collectionTime = mp.getCollectionTime();

			// .. Draw allocated and used strings ..
			big.setColor(Color.green);

			// Print Max memory allocated for this memory pool.
			big.drawString(String.valueOf(collectionTime) + "ms coltime ",
					x1 + 4.0f, (float) y1 + ascent + 0.5f);
			big.setColor(Color.yellow);

			// Print the memory pool name.
			big.drawString(mp.getName(), x1 + x2 / 2, (float) y1 + ascent
					+ 0.5f);

			// Print the memory used by this memory pool.
			usedStr = String.valueOf(collectionCount) + "count";
			big.setColor(Color.green);
			big.drawString(usedStr, x1 + 4, y1 + y2 - descent);

			// Calculate remaining size
			float ssH = ascent + descent;
			float remainingHeight = (float) (y2 - (ssH * 2) - 0.5f);
			float blockHeight = remainingHeight / 10;
			float blockWidth = 20.0f;
			float remainingWidth = (float) (x2 - blockWidth - 10);

			// .. Memory Free ..
			big.setColor(mfColor);
			int MemUsage = 0;
			if (collectionCount > 0){
				MemUsage=(int) ((collectionTime / collectionCount) * 10);
			}
			int i = 0;
			for (; i < MemUsage; i++) {
				mfRect.setRect(x1 + 5, (float) y1 + ssH + i * blockHeight,
						blockWidth, (float) blockHeight - 1);
				big.fill(mfRect);
			}

			// .. Memory Used ..
			big.setColor(Color.green);
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
			if (usedMem[npool] == null) {
				usedMem[npool] = new float[usageHistCount];
				ptNum[npool] = 0;
			}

			// save memory usage history.
			usedMem[npool][ptNum[npool]] = collectionCount;

			big.setColor(Color.yellow);

			int w1; // width of memory usage history.
			if (ptNum[npool] > graphW) {
				w1 = graphW;
			} else {
				w1 = ptNum[npool];
			}

			for (int j = graphX + graphW - w1, k = ptNum[npool] - w1; k < ptNum[npool]; k++, j++) {
				if (k != 0) {
					if (usedMem[npool][k] != usedMem[npool][k - 1]) {
						int h1 = (int) (graphY + graphH
								* ((collectionTime - usedMem[npool][k - 1]) / collectionTime));
						int h2 = (int) (graphY + graphH
								* ((collectionTime - usedMem[npool][k]) / collectionTime));
						big.drawLine(j - 1, h1, j, h2);
					} else {
						int h1 = (int) (graphY + graphH
								* ((collectionTime - usedMem[npool][k]) / collectionTime));
						big.fillRect(j, h1, 1, 1);
					}
				}
			}
			if (ptNum[npool] + 2 == usedMem[npool].length) {
				// throw out oldest point
				for (int j = 1; j < ptNum[npool]; j++) {
					usedMem[npool][j - 1] = usedMem[npool][j];
				}
				--ptNum[npool];
			} else {
				ptNum[npool]++;
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

			Thread me = Thread.currentThread();

			while (thread == me && !isShowing() || getSize().width == 0) {
				try {
					thread.sleep(500);
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
					thread.sleep(sleepAmount);
				} catch (InterruptedException e) {
					break;
				}
				if (GCMonitor.dateStampCB.isSelected()) {
					System.out.println(new Date().toString() + " " + usedStr);
				}
			}
			thread = null;
		}
	}

	// Test thread to consume memory
	static class Memeater extends ClassLoader implements Runnable {
		Object y[];

		public Memeater() {
		}

		public void run() {
			y = new Object[10000000];
			int k = 0;
			while (true) {
				if (k == 5000000)
					k = 0;
				y[k++] = new Object();
				try {
					Thread.sleep(20);
				} catch (Exception x) {
				}

				// to consume perm gen storage
				try {
					// the classes are small so we load 10 at a time
					for (int i = 0; i < 10; i++) {
						loadNext();
					}
				} catch (ClassNotFoundException x) {
					// ignore exception
				}

			}

		}

		Class loadNext() throws ClassNotFoundException {

			// public class TestNNNNNN extends java.lang.Object{
			// public TestNNNNNN();
			// Code:
			// 0: aload_0
			// 1: invokespecial #1; //Method java/lang/Object."<init>":()V
			// 4: return
			// }

			int begin[] = { 0xca, 0xfe, 0xba, 0xbe, 0x00, 0x00, 0x00, 0x30,
					0x00, 0x0a, 0x0a, 0x00, 0x03, 0x00, 0x07, 0x07, 0x00, 0x08,
					0x07, 0x00, 0x09, 0x01, 0x00, 0x06, 0x3c, 0x69, 0x6e, 0x69,
					0x74, 0x3e, 0x01, 0x00, 0x03, 0x28, 0x29, 0x56, 0x01, 0x00,
					0x04, 0x43, 0x6f, 0x64, 0x65, 0x0c, 0x00, 0x04, 0x00, 0x05,
					0x01, 0x00, 0x0a, 0x54, 0x65, 0x73, 0x74 };

			int end[] = { 0x01, 0x00, 0x10, 0x6a, 0x61, 0x76, 0x61, 0x2f, 0x6c,
					0x61, 0x6e, 0x67, 0x2f, 0x4f, 0x62, 0x6a, 0x65, 0x63, 0x74,
					0x00, 0x21, 0x00, 0x02, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00,
					0x00, 0x01, 0x00, 0x01, 0x00, 0x04, 0x00, 0x05, 0x00, 0x01,
					0x00, 0x06, 0x00, 0x00, 0x00, 0x11, 0x00, 0x01, 0x00, 0x01,
					0x00, 0x00, 0x00, 0x05, 0x2a, 0xb7, 0x00, 0x01, 0xb1, 0x00,
					0x00, 0x00, 0x00, 0x00, 0x00 };

			// TestNNNNNN

			String name = "Test" + Integer.toString(count++);

			byte value[];
			try {
				value = name.substring(4).getBytes("UTF-8");
			} catch (java.io.UnsupportedEncodingException x) {
				throw new Error();
			}

			// construct class file

			int len = begin.length + value.length + end.length;
			byte b[] = new byte[len];
			int i, pos = 0;
			for (i = 0; i < begin.length; i++) {
				b[pos++] = (byte) begin[i];
			}
			for (i = 0; i < value.length; i++) {
				b[pos++] = value[i];
			}
			for (i = 0; i < end.length; i++) {
				b[pos++] = (byte) end[i];
			}

			return defineClass(name, b, 0, b.length);

		}

		static int count = 100000;

	}

	public static void main(String s[]) {
		final GCMonitor demo = new GCMonitor();
		WindowListener l = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
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
		f.setSize(new Dimension(400, 500));
		f.setVisible(true);
		demo.surf.start();
		Thread thr = new Thread(new Memeater());
		thr.start();
	}

}
