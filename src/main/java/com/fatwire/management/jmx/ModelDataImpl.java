/**
 * 
 */
package com.fatwire.management.jmx;

import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;

class MemoryPoolMXBeanModelData implements ModelData<Float> {
    private final MemoryPoolMXBean model;

    private float max = -1;

    private float used = 0;

    private String name;
    private History<Float> history = new History<Float>(100000);

    /**
     * @param model
     */
    public MemoryPoolMXBeanModelData(MemoryPoolMXBean model) {
        super();
        this.model = model;
        name = model.getName() + " "
                + (model.getType() == MemoryType.HEAP ? "heap" : "non-heap");
    }

    public Float getValue() {
        return used;
    }

    public Float getMax() {
        return max;
    }

    public String getName() {
        return name;
    }

    @Override
    public void refreshData() {
        float u = model.getUsage().getUsed();
        max = Math.max(max, u);
        used = u;
        history.add(u);
        if (max == -1) {
            max = model.getUsage().getMax();
        }
    }

    /**
     * @return the history
     */
    public History<Float> getHistory() {
        return history;
    }
}