package org.cerion.stockcharts.charts;

import android.text.TextUtils;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.cerion.stocklist.arrays.BandArray;
import org.cerion.stocklist.arrays.FloatArray;
import org.cerion.stocklist.arrays.ValueArray;
import org.cerion.stocklist.model.Overlay;

import java.util.ArrayList;
import java.util.List;

class OverlayDataSet {

    private ValueArray mValues;
    private Overlay mType;
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

    OverlayDataSet(Overlay type, Number ...params) {
        mParams = params;
        mType = type;
        mColor = Colors.getOverlay(i++);

        p1 = params.length > 0 ? params[0] : null;
        p2 = params.length > 1 ? params[1] : null;
        p3 = params.length > 2 ? params[2] : null;
    }

    static OverlayDataSet getEMA(int period) {
        return new OverlayDataSet(Overlay.EMA, period);
    }

    static OverlayDataSet getSMA(int period) {
        return new OverlayDataSet(Overlay.SMA, period);
    }

    static OverlayDataSet getBB(int period, float multiplier) {
        return new OverlayDataSet(Overlay.BB, period, multiplier);
    }

    static OverlayDataSet getKAMA(int p1, int p2, int p3) {
        return new OverlayDataSet(Overlay.KAMA, p1, p2, p3);
    }

    public Overlay getType() {
        return mType;
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

    private ValueArray eval() {
        FloatArray values = (FloatArray) mValues;
        switch (mType) {
            case SMA: return values.sma((int)p1);
            case EMA: return values.ema((int)p1);
            case BB: return values.bb((int)p1,(float)p2);
            case KAMA: return values.kama((int)p1,(int)p2,(int)p3);
            case LINE: return values.line((float)p1);
            case LINREG: return values.linearRegressionLine();
        }

        return null;
    }

    public String getLabel() {
        return mType.name() + " " + TextUtils.join(",", mParams);
        /*
        switch(mType) {
            case SMA: return "SMA " + p1;
            case EMA: return "EMA " + p1;
            case BB: return "BB " + p1 + "," + p2;
            case KAMA: return "KAMA " + p1 + "," + p2 + "," + p3;
            case LINE: return "LINE " + p1;
        }

        return "";
        */
    }

    public int getColor() {
        return mColor;
    }

    public List<LineDataSet> getDataSets(ValueArray arr) {
        mValues = arr;
        if(mType == Overlay.BB)
            return getMultiDataSet();

        List<LineDataSet> sets = new ArrayList<>();
        sets.add( getSingleDataSet() );
        return sets;
    }

    // TODO allow band Array to get FloatArray for upper and lowers
    // TODO add function to get LineDataSet based on this array
    private LineDataSet getSingleDataSet() {
        ArrayList<Entry> entries = new ArrayList<>();
        ValueArray values = eval();

        FloatArray f_values = (FloatArray)values;
        for (int i = 0; i < values.size(); i++)
            entries.add(new Entry(i, f_values.get(i)));

        LineDataSet set = new LineDataSet(entries, getLabel());
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setColor(mColor);

        return set;
    }

    private List<LineDataSet> getMultiDataSet() {
        ArrayList<Entry> entries1 = new ArrayList<>();
        ArrayList<Entry> entries2 = new ArrayList<>();

        BandArray values = (BandArray)eval();

        for (int i = 0; i < values.size(); i++) {
            entries1.add(new Entry(i, values.lower(i)));
            entries2.add(new Entry(i, values.upper(i)));
        }

        List<LineDataSet> sets = new ArrayList<>();
        sets.add(new LineDataSet(entries1, getLabel()));
        sets.add(new LineDataSet(entries2, getLabel()));

        for(LineDataSet set : sets) {
            set.setDrawCircles(false);
            set.setDrawValues(false);
            set.setColor(mColor);
        }

        return sets;
    }
}
