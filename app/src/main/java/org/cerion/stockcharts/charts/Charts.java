package org.cerion.stockcharts.charts;

import android.content.Context;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.cerion.stocklist.data.FloatArray;

import java.util.ArrayList;
import java.util.List;

public class Charts {

    public static final int CHART_HEIGHT = 1000;


    public static Chart getLineChart(Context context, FloatArray base, List<String> dates, String label, List<Overlay> overlays)
    {
        LineChart chart = new LineChart(context);
        chart.setMinimumHeight(CHART_HEIGHT);

        ArrayList<Entry> entries = new ArrayList<>();
        for(int i = 0; i < base.size(); i++) {
            entries.add(new Entry(base.get(i), i));
        }

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);

        ArrayList<LineDataSet> sets = new ArrayList<>();
        sets.add(dataSet);

        for(Overlay overlay : overlays) {
            sets.addAll(overlay.getDataSets(base));
        }

        LineData lineData = new LineData(dates, sets);
        chart.setData(lineData);

        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        chart.setDescription("");

        return chart;
    }


}
