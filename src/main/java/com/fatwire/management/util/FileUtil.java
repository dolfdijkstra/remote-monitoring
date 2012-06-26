package com.fatwire.management.util;

import java.util.regex.Pattern;

public class FileUtil {
    private static Pattern p = Pattern.compile("[\\?\\[\\]/\\\\=\\+<>:;\",\\*|]");
    
    public static String filenameSafeString(String s){
        return p.matcher(s).replaceAll("_");
    }
    
    
}
