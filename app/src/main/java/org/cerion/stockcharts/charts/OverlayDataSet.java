package org.cerion.stockcharts.charts;

import android.graphics.Color;
import android.text.TextUtils;

import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.arrays.BandArray;
import org.cerion.stocklist.arrays.FloatArray;
import org.cerion.stocklist.arrays.PairArray;
import org.cerion.stocklist.arrays.ValueArray;
import org.cerion.stocklist.functions.FunctionCall;
import org.cerion.stocklist.functions.IFunction;
import org.cerion.stocklist.functions.Overlay;
import org.cerion.stocklist.functions.PriceOverlay;

import java.util.ArrayList;
import java.util.List;

class OverlayDataSet {

    private IFunction mType;
    private Number p1;
    private Number p2;
    private Number p3;
    private int mColor;
    private Number[] mParams;

    private static int i = 0;

    OverlayDataSet(IFunction type, Number ...params) {
        if(type.getClass() != Overlay.class && type.getClass() != PriceOverlay.class)
            throw new RuntimeException("IFunction type must be an overlay");

        mParams = params;
        mType = type;
        mColor = Colors.getOverlay(i++);

        p1 = params.length > 0 ? params[0] : null;
        p2 = params.length > 1 ? params[1] : null;
        p3 = params.length > 2 ? params[2] : null;
    }

    String getLabel() {
        if(mType.getClass() == Overlay.class)
            return ((Overlay)mType).name() + " " + TextUtils.join(",", mParams);

        return ((PriceOverlay)mType).name() + " " + TextUtils.join(",", mParams);
    }

    public int getColor() {
        return mColor;
    }

}
