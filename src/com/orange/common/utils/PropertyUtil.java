package com.orange.common.utils;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-6-23
 * Time: 下午2:15
 * To change this template use File | Settings | File Templates.
 */
public class PropertyUtil {
    public static String getStringProperty(String para, String defaultValue) {
        String value = System.getProperty(para);
        if (value == null)
            return defaultValue;
        else
            return value;
    }

    public static int getIntProperty(String para, int defaultValue) {
        String value = System.getProperty(para);
        if (value == null || value.length() == 0)
            return defaultValue;
        else
            return Integer.parseInt(value);
    }
}
