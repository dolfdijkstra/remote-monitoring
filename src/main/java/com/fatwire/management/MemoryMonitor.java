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
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Demo code which plots the memory usage by all memory pools. The memory usage
 * is sampled at some time interval using java.lang.management API. This demo
 * code is modified based java2d MemoryMonitor demo.
 */
public class MemoryMonitor extends JPanel {
    interface Model {

        long getUsed();

        long getMax();

        String getName();

        int getNumberOfHistoryPoints();

        float[] getUsedMem();

        void updateHistory();
    }

    /**
     * 
     */
    private static final long serialVersionUID = 5398155452623231466L;

    public GraphSurface surf;

    public MemoryMonitor() {
        setLayout(new BorderLayout());
        add(surf = new GraphSurface());
    }

    public static void main(final String s[]) {
        final MemoryMonitor demo = new MemoryMonitor();
        final ModelUpdater modelUpdater = new ModelUpdater();
        final WindowListener l = new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                demo.surf.stop();
                modelUpdater.stop();
                System.exit(0);
            }

            @Override
            public void windowDeiconified(final WindowEvent e) {
                demo.surf.start();
            }

            @Override
            public void windowIconified(final WindowEvent e) {
                demo.surf.stop();
            }
        };
        final JFrame f = new JFrame("MemoryMonitor");
        f.addWindowListener(l);
        f.getContentPane().add("Center", demo);
        // Get memory pools.
        //java.util.List<MemoryPoolMXBean> mpools = ;

        
        for (final MemoryPoolMXBean mp : ManagementFactory
                .getMemoryPoolMXBeans()) {
            Model m = new Model() {
                private final float[] usedMem = new float[2000];

                private int numberOfHistoryPoints = 0;

                long max;

                long used;

                @Override
                public long getMax() {
                    return max;
                }

                @Override
                public long getUsed() {
                    return used;
                }

                @Override
                public String getName() {
                    return mp.getName()
                            + (mp.getType() == MemoryType.HEAP ? "(H)" : "(N)");
                }

                public void updateHistory() {
                    max = mp.getUsage().getMax();
                    used = mp.getUsage().getUsed();
                    // Plot memory usage by this memory pool.

                    // save memory usage history.
                    usedMem[numberOfHistoryPoints] = used;
                    if (numberOfHistoryPoints + 2 == usedMem.length) {
                        // throw out oldest point
                        for (int j = 1; j < numberOfHistoryPoints; j++) {
                            usedMem[j - 1] = usedMem[j];
                        }
                        --numberOfHistoryPoints;
                    } else {
                        numberOfHistoryPoints++;
                    }

                }

                @Override
                public int getNumberOfHistoryPoints() {
                    return this.numberOfHistoryPoints;
                }

                @Override
                public float[] getUsedMem() {
                    return this.usedMem;
                }

            };
            modelUpdater.addModel(m);
            demo.surf.addModel(m);
        }

        modelUpdater.start();
        f.pack();
        f.setSize(new Dimension(800, 500));
        f.setVisible(true);
        demo.surf.start();

        //final Thread thr = new Thread(new Memeater());
        //thr.start();
    }

    static class ModelUpdater implements Runnable {
        private java.util.List<Model> models = new LinkedList<Model>();

        private Thread thread;

        public void addModel(Model m) {
            models.add(m);
        }

        public void start() {
            thread = new Thread(this);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setName("ModelUpdater");
            thread.start();
        }

        public synchronized void stop() {
            thread = null;
            notify();
        }

        public void run() {

            final Thread me = Thread.currentThread();

            while (thread == me) {
                for (Model model : models) {
                    model.updateHistory();
                }
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    break;
                }
            }
            thread = null;
        }
    }

}
