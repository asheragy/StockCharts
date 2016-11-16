package org.cerion.stockcharts.charts;


import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import org.cerion.stocklist.arrays.BandArray;
import org.cerion.stocklist.arrays.FloatArray;
import org.cerion.stocklist.arrays.ValueArray;
import org.cerion.stocklist.arrays.VolumeArray;

import java.util.ArrayList;
import java.util.List;

public class Overlay {

    public static final int TYPE_EMA = 0;
    public static final int TYPE_SMA = 1;
    public static final int TYPE_BB = 2;
    public static final int TYPE_KAMA = 3;

    private ValueArray mValues;
    private int mType;
    private int p1;
    private float f1;
    private int p2;
    private int p3;
    private int mColor;

    private boolean isVolume()
    {
        if(mValues.getClass() == VolumeArray.class)
            return true;

        return false;
    }

    private static int i = 0;
    private Overlay(int type) {
        mType = type;

        if(i % 3 == 0)
            mColor = Color.RED;
        else if(i % 3 == 1)
            mColor = Color.BLUE;
        else
            mColor = Color.GREEN;

        i++;
    }

    public static Overlay getEMA(int period) {
        Overlay result = new Overlay(TYPE_EMA);
        result.p1 = period;
        return result;
    }

    public static Overlay getSMA(int period) {
        Overlay result = new Overlay(TYPE_SMA);
        result.p1 = period;
        return result;
    }

    public static Overlay getBB(int period, float multiplier)
    {
        Overlay result = new Overlay(TYPE_BB);
        result.p1 = period;
        result.f1 = multiplier;
        return result;
    }

    public static Overlay getKAMA(int p1, int p2, int p3) {
        Overlay result = new Overlay(TYPE_KAMA);
        result.p1 = p1;
        result.p2 = p2;
        result.p3 = p3;
        return result;
    }

    public int getType() {
        return mType;
    }

    public ValueArray eval()
    {
        if(isVolume())
        {
            VolumeArray values = (VolumeArray) mValues;
            switch (mType) {
                case TYPE_SMA: return values.sma(p1);
                case TYPE_EMA: return values.ema(p1);
            }
        } else {

            FloatArray values = (FloatArray) mValues;
            switch (mType) {
                case TYPE_SMA: return values.sma(p1);
                case TYPE_EMA: return values.ema(p1);
                case TYPE_BB: return values.bb(p1,f1);
                case TYPE_KAMA: return values.kama(p1,p2,p3);
            }
        }

        return null;
    }

    public String getLabel() {

        switch(mType) {
            case TYPE_SMA: return "SMA " + p1;
            case TYPE_EMA: return "EMA " + p1;
            case TYPE_BB: return "BB " + p1 + "," + f1;
            case TYPE_KAMA: return "KAMA " + p1 + "," + f1;
        }

        return "";
    }

    public int getColor() {
        return mColor;
    }

    public List<LineDataSet> getDataSets(ValueArray arr) {
        mValues = arr;
        if(mType == TYPE_BB)
            return getMultiDataSet();

        List<LineDataSet> sets = new ArrayList<>();
        sets.add( getSingleDataSet() );
        return sets;
    }

    private LineDataSet getSingleDataSet() {
        ArrayList<Entry> entries = new ArrayList<>();
        ValueArray values = eval();

        if(!isVolume()) {
            FloatArray f_values = (FloatArray)values;
            for (int i = 0; i < values.size(); i++)
                entries.add(new Entry(f_values.get(i), i));
        } else {
            VolumeArray L_values = (VolumeArray)values;
            for (int i = 0; i < values.size(); i++)
                entries.add(new Entry(L_values.get(i), i));
        }

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
            entries1.add(new Entry(values.lower(i), i));
            entries2.add(new Entry(values.upper(i), i));
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
