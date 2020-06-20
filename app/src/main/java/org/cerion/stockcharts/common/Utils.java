package org.cerion.stockcharts.common;

import java.text.DateFormat;
import java.text.DecimalFormat;

public class Utils {

    public static final DateFormat dateFormatShort = DateFormat.getDateInstance(DateFormat.SHORT);
    public static final DateFormat dateFormatLong = DateFormat.getDateInstance(DateFormat.LONG);

    public static final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    public static final DecimalFormat decimalFormat3 = new DecimalFormat("0.000");

    /**
     * Formats decimal to 3 places unless it has no zeros otherwise treats as integer to string
     * @param num number to format
     * @return double rounded to 3 digits or decimal portion dropped
     */
    public static String getDecimalFormat3(double num) {
        if(num % 1 == 0)
            return (int)num + "";
        else
            return decimalFormat3.format(num);
    }

    // TODO in stocklib add function to get this directly in list and quote for several ranges
    public static int highLowRange(float current, float high, float low) {
        float range = high - low;
        float diff = current - low;
        float percent = diff / range;

        return (int)(percent * 100);
    }
}