package org.cerion.stockcharts.charts;


import android.graphics.Color;
import android.graphics.Paint;

import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.cerion.stocklist.Price;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.arrays.BandArray;
import org.cerion.stocklist.arrays.FloatArray;
import org.cerion.stocklist.arrays.MACDArray;
import org.cerion.stocklist.arrays.PairArray;

import java.util.ArrayList;
import java.util.List;

class DataSetConverter {

    private static class EntrySet {
        EntrySet(int color) {
            this.color = color;
        }

        List<Entry> entries = new ArrayList<>();
        int color = Color.BLACK;
    }

    static ICandleDataSet getCandleDataSet(PriceList list, boolean logscale) {
        ArrayList<CandleEntry> entries = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Price p = list.get(i);
            if(logscale)
                entries.add(new CandleEntry(i, (float)Math.log(p.high+1), (float)Math.log(p.low+1), (float)Math.log(p.open+1), (float)Math.log(p.close+1))); // order is high, low, open, close
            else
                entries.add(new CandleEntry(i, p.high, p.low, p.open, p.close)); // order is high, low, open, close
        }

        CandleDataSet dataSet = new CandleDataSet(entries, "");
        dataSet.setDrawValues(false);
        dataSet.setDecreasingColor(Colors.CANDLE_DOWN);
        dataSet.setDecreasingPaintStyle(Paint.Style.FILL);
        dataSet.setIncreasingColor(Colors.CANDLE_UP);
        dataSet.setIncreasingPaintStyle(Paint.Style.FILL);

        return dataSet;
    }


    static List<ILineDataSet> getSingleDataSet(FloatArray values, int color) {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++)
            entries.add(new Entry(i, values.get(i)));

        // TODO refactor to EntrySet
        LineDataSet set = new LineDataSet(entries, "");
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setColor(color);

        List<ILineDataSet> result = new ArrayList<>();
        result.add(set);
        return result;
    }

    static List<ILineDataSet> getBandDataSet(BandArray values, int color) {
        ArrayList<Entry> entries1 = new ArrayList<>();
        ArrayList<Entry> entries2 = new ArrayList<>();

        for (int i = 0; i < values.size(); i++) {
            entries1.add(new Entry(i, values.lower(i)));
            entries2.add(new Entry(i, values.upper(i)));
        }

        List<ILineDataSet> sets = new ArrayList<>();
        sets.add(new LineDataSet(entries1, ""));
        sets.add(new LineDataSet(entries2, ""));

        for(ILineDataSet set : sets) {
            ((LineDataSet)set).setDrawCircles(false);
            set.setDrawValues(false);
            ((LineDataSet)set).setColor(color);
        }

        return sets;
    }

    static List<ILineDataSet> getPairDataSet(PairArray arr, int positiveColor, int negativeColor) {
        EntrySet pos = new EntrySet(positiveColor);
        EntrySet neg = new EntrySet(negativeColor);

        for (int i = 0; i < arr.size(); i++) {
            pos.entries.add(new Entry(i, arr.getPos(i)));
            neg.entries.add(new Entry(i, arr.getNeg(i)));
        }

        return getLineDataSets(pos, neg);
    }

    static List<ILineDataSet> getMACDDataSet(MACDArray arr, int macdColor, int signalColor, int histColor) {
        EntrySet macd = new EntrySet(macdColor);
        EntrySet signal = new EntrySet(Color.RED);
        EntrySet hist = new EntrySet(Color.BLUE);

        for (int i = 0; i < arr.size(); i++) {
            macd.entries.add(  new Entry(i, arr.get(i)));
            signal.entries.add(new Entry(i, arr.signal(i)));
            hist.entries.add(  new Entry(i, arr.hist(i)));
        }

        return getLineDataSets(macd, signal, hist);
    }

    static List<ILineDataSet> getLineDataSets(EntrySet...entrySets) {
        List<ILineDataSet> sets = new ArrayList<>();

        for(EntrySet entrySet : entrySets) {
            LineDataSet dataSet = new LineDataSet(entrySet.entries, "");
            dataSet.setDrawCircles(false);
            dataSet.setDrawValues(false);
            dataSet.setColor(entrySet.color);
            sets.add(dataSet);
        }

        return sets;
    }
}
