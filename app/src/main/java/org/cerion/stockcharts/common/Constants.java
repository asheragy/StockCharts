package org.cerion.stockcharts.common;

import java.util.Date;

public class Constants {

    public static final int MAX_DAILY = 250 * 5;
    public static final int MAX_WEEKLY = 52 * 10;
    public static final int MAX_MONTHLY = 12 * 20;
    public static final int MAX_QUARTERLY = 12 * 50;

    public static final Date START_DATE_DAILY = new Date(2012 - 1900, 0, 1);
    public static final Date START_DATE_WEEKLY = new Date(2005 - 1900, 0, 1);
    public static final Date START_DATE_MONTHLY = new Date(1970 - 1900, 0, 1);
}
