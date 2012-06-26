package com.fatwire.management.jmx;

import java.util.LinkedList;

public class History<T> {

    private LinkedList<T> history;

    private final int maxCount;

    History(int max) {
        history = new LinkedList<T>();
        this.maxCount = max;
    }

    void add(T num) {
        if (history.size() == maxCount) {
            history.removeFirst();
        }
        history.addLast(num);

    }

    T get(int i) {
        return history.get(i);
    }

    public int getCount() {
        return history.size();
    }

}
