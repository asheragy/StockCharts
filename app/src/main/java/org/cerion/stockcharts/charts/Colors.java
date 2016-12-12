package org.cerion.stockcharts.charts;

import android.graphics.Color;

public class Colors {

    private static int OVERLAYS[] = { Color.RED, Color.BLUE, Color.GREEN };
    public static final int CANDLE_UP = Color.GREEN;
    public static final int CANDLE_DOWN = Color.RED;

    static int getOverlay(int index) {
        return OVERLAYS[index % OVERLAYS.length];
    }


}
