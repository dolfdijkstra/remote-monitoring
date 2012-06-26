/**
 * 
 */
package com.fatwire.management.jmx;

interface ModelData<T> {

    History getHistory();
    
    T getValue();

    String getName();

    T getMax();

    void refreshData() throws Exception;

}