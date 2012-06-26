/**
 * 
 */
package com.fatwire.management;

import java.util.Arrays;

class Memeater extends ClassLoader implements Runnable {
    char[][] y;

    public Memeater() {
    }

    public void run() {
        y = new char[10000000][];
        int k = 0;
        while (true) {
            if (k == 5000000) {
                k = 0;
            }
            y[k] = new char[2000];
            Arrays.fill(y[k++], 'x');

            try {
                Thread.sleep(20);
            } catch (final Exception x) {
            }

            // to consume perm gen storage
            try {
                // the classes are small so we load 10 at a time
                for (int i = 0; i < 10; i++) {
                    loadNext();
                }
            } catch (final ClassNotFoundException x) {
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

        final int begin[] = { 0xca, 0xfe, 0xba, 0xbe, 0x00, 0x00, 0x00,
                0x30, 0x00, 0x0a, 0x0a, 0x00, 0x03, 0x00, 0x07, 0x07, 0x00,
                0x08, 0x07, 0x00, 0x09, 0x01, 0x00, 0x06, 0x3c, 0x69, 0x6e,
                0x69, 0x74, 0x3e, 0x01, 0x00, 0x03, 0x28, 0x29, 0x56, 0x01,
                0x00, 0x04, 0x43, 0x6f, 0x64, 0x65, 0x0c, 0x00, 0x04, 0x00,
                0x05, 0x01, 0x00, 0x0a, 0x54, 0x65, 0x73, 0x74 };

        final int end[] = { 0x01, 0x00, 0x10, 0x6a, 0x61, 0x76, 0x61, 0x2f,
                0x6c, 0x61, 0x6e, 0x67, 0x2f, 0x4f, 0x62, 0x6a, 0x65, 0x63,
                0x74, 0x00, 0x21, 0x00, 0x02, 0x00, 0x03, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x04, 0x00, 0x05, 0x00,
                0x01, 0x00, 0x06, 0x00, 0x00, 0x00, 0x11, 0x00, 0x01, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x05, 0x2a, 0xb7, 0x00, 0x01, 0xb1,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        // TestNNNNNN

        final String name = "Test" + Integer.toString(count++);

        byte value[];
        try {
            value = name.substring(4).getBytes("UTF-8");
        } catch (final java.io.UnsupportedEncodingException x) {
            throw new Error();
        }

        // construct class file

        final int len = begin.length + value.length + end.length;
        final byte b[] = new byte[len];
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