package com.fatwire.management.jmx;

import java.io.File;
import java.io.FileFilter;

public class Rename {

    static class Walker {

        FileFilter ff = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory()
                        || pathname.getName().toLowerCase().endsWith(".xml");
            }

        };

        void walk(File file) {

            if (file.isDirectory()) {
                for (File child : file.listFiles(ff)) {
                    walk(child);
                }

            } else if (file.getName().indexOf("MetricFactory") > 2) {
                
                file.renameTo( new File(file.getParentFile(), file.getName().replace("MetricFactory", "")));
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        File f = new File("conf/template");
        new Walker().walk(f);

    }
}
