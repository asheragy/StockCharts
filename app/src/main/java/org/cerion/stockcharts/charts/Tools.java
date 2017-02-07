package org.cerion.stockcharts.charts;


import android.graphics.Color;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.cerion.stocklist.arrays.BandArray;
import org.cerion.stocklist.arrays.PairArray;

import java.util.ArrayList;
import java.util.List;

class Tools {

    private static class EntrySet {
        EntrySet() {}
        EntrySet(int color) {
            this.color = color;
        }

        List<Entry> entries = new ArrayList<>();
        int color = Color.BLACK;
    }

    public static List<ILineDataSet> getPairDataSet(PairArray arr, int positiveColor, int negativeColor) {
        EntrySet pos = new EntrySet(positiveColor);
        EntrySet neg = new EntrySet(negativeColor);

        for (int i = 0; i < arr.size(); i++) {
            pos.entries.add(new Entry(i, arr.getPos(i)));
            neg.entries.add(new Entry(i, arr.getNeg(i)));
        }

        return getLineDataSets(pos, neg);
    }

    private static List<ILineDataSet> getLineDataSets(EntrySet...entrySets) {
        List<ILineDataSet> sets = new ArrayList<>();

        for(EntrySet entrySet : entrySets) {
            LineDataSet dataSet = new LineDataSet(entrySet.entries, ""); // Ignore label, its set later
            dataSet.setDrawCircles(false);
            dataSet.setDrawValues(false);
            dataSet.setColor(entrySet.color);
            sets.add(dataSet);
        }

        return sets;
    }
}
