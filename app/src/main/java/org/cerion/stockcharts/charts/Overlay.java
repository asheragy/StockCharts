package org.cerion.stockcharts.charts;


import android.graphics.Color;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import org.cerion.stocklist.data.BandArray;
import org.cerion.stocklist.data.FloatArray;
import org.cerion.stocklist.data.ValueArray;
import org.cerion.stocklist.data.VolumeArray;

import java.util.ArrayList;
import java.util.List;

public class Overlay {

    public static final int TYPE_EMA = 0;
    public static final int TYPE_SMA = 1;
    public static final int TYPE_BB = 2;


    private ValueArray mValues;
    private int mType;
    private int p1;
    private float f1;

    private boolean isVolume()
    {
        if(mValues.getClass() == VolumeArray.class)
            return true;

        return false;
    }

    private Overlay(int type) {
        mType = type;
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

            }
        }

        return null;
    }

    private String getLabel() {

        switch(mType) {
            case TYPE_SMA: return "SMA " + p1;
            case TYPE_EMA: return "EMA " + p1;
            case TYPE_BB: return "BB " + p1 + "," + f1;
        }

        return "";
    }

    public List<LineDataSet> getDataSets(ValueArray arr)
    {
        mValues = arr;
        if(mType == TYPE_BB)
            return getMultiDataSet();

        List<LineDataSet> sets = new ArrayList<>();
        sets.add( getSingleDataSet() );
        return sets;
    }


    static int i = 0;
    private LineDataSet getSingleDataSet()
    {
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

        if(i == 0)
            set.setColor(Color.RED);
        else if(i == 1)
            set.setColor(Color.BLUE);
        else
            set.setColor(Color.GREEN);

        i++;

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
            set.setColor(Color.LTGRAY);
        }


        return sets;
    }
}
