package org.cerion.stockcharts.charts;

import android.graphics.Color;
import android.text.TextUtils;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineRadarDataSet;

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
import java.util.Arrays;
import java.util.List;

class OverlayDataSet {

    //private ValueArray mValues;
    private IFunction mType;
    private Number p1;
    private Number p2;
    private Number p3;
    private int mColor;
    private Number[] mParams;

    private static int i = 0;

    // TODO better way of doing this
    public static void resetColors() {
        i = 0;
    }

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

    public static List<ILineDataSet> getLineDataSets(PriceList base, List<OverlayDataSet> overlays) {
        List<ILineDataSet> sets = new ArrayList<>();
        if(overlays != null) {
            for (OverlayDataSet overlay : overlays) {

                if(overlay.mType.getClass() == Overlay.class)
                    sets.addAll(overlay.getDataSets(base.getClose()));
                else
                    sets.addAll(overlay.getDataSets(base));
            }
        }

        return sets;
    }

    public static List<ILineDataSet> getLineDataSets(FloatArray base, List<OverlayDataSet> overlays) {
        List<ILineDataSet> sets = new ArrayList<>();
        if(overlays != null) {
            for (OverlayDataSet overlay : overlays) {
                sets.addAll(overlay.getDataSets(base));
            }
        }

        return sets;
    }

    private ValueArray eval(FloatArray values) {

        if(mType.getClass() == Overlay.class) {

            Overlay overlay = (Overlay)mType;
            switch (overlay) {
                case SMA:
                    return values.sma((int) p1);
                case EMA:
                    return values.ema((int) p1);
                case BB:
                    return values.bb((int) p1, (float) p2);
                case KAMA:
                    return values.kama((int) p1, (int) p2, (int) p3);
                case LINE:
                    return values.line((float) p1);
                case LINREG:
                    return values.linearRegressionLine();
            }
        }

        return null;
    }

    public String getLabel() {
        if(mType.getClass() == Overlay.class)
            return ((Overlay)mType).name() + " " + TextUtils.join(",", mParams);

        return ((PriceOverlay)mType).name() + " " + TextUtils.join(",", mParams);
    }

    public int getColor() {
        return mColor;
    }

    private List<ILineDataSet> getDataSets(PriceList list) {
        FunctionCall call = new FunctionCall(mType, mParams);
        ValueArray result = call.eval(list);
        return getDataSets(result);
    }

    private List<ILineDataSet> getDataSets(FloatArray arr) {
        return getDataSets(eval(arr));
    }

    private List<ILineDataSet> getDataSets(ValueArray arr) {
        if(arr.getClass() == BandArray.class)
            return getBandDataSet((BandArray)arr);
        if(arr.getClass() == PairArray.class)
            return Tools.getPairDataSet((PairArray)arr, Color.GREEN, Color.RED);

        return getSingleDataSet((FloatArray)arr);
    }

    private List<ILineDataSet> getSingleDataSet(FloatArray values) {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++)
            entries.add(new Entry(i, values.get(i)));

        LineDataSet set = new LineDataSet(entries, getLabel());
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setColor(mColor);

        List<ILineDataSet> result = new ArrayList<>();
        result.add(set);
        return result;
    }

    // TODO refactor to new class and share similar code in ChartFactory
    private List<ILineDataSet> getBandDataSet(BandArray values) {
        ArrayList<Entry> entries1 = new ArrayList<>();
        ArrayList<Entry> entries2 = new ArrayList<>();

        for (int i = 0; i < values.size(); i++) {
            entries1.add(new Entry(i, values.lower(i)));
            entries2.add(new Entry(i, values.upper(i)));
        }

        List<ILineDataSet> sets = new ArrayList<>();
        sets.add(new LineDataSet(entries1, getLabel()));
        sets.add(new LineDataSet(entries2, getLabel()));

        for(ILineDataSet set : sets) {
            ((LineDataSet)set).setDrawCircles(false);
            set.setDrawValues(false);
            ((LineDataSet)set).setColor(mColor);
        }

        return sets;
    }
}
