package com.fatwire.management.rrd;

import com.fatwire.management.JMXMetric;

public class RrdUtil {
    public static String toRrdName(JMXMetric metric) {
        return metric.getObjName().getDomain() + "/" + metric.getDescription();
    }
    /**
     * Truncactes the name to a string of max 20 chars, if more then 20 chars are found the last 3 are replaced with '...'.
     * 
     * @param name
     * @return the truncate name or the original
     */

    public static String truncateDsName(String name) {
        if (name.length() > 20) {
            return name.substring(0, 17) + "...";
        }
        return name;

    }

}
