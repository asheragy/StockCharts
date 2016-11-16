package org.cerion.stockcharts.charts;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.cerion.stocklist.Price;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.arrays.BandArray;
import org.cerion.stocklist.arrays.FloatArray;
import org.cerion.stocklist.arrays.MACDArray;
import org.cerion.stocklist.arrays.PairArray;
import org.cerion.stocklist.arrays.ValueArray;
import org.cerion.stocklist.arrays.VolumeArray;
import org.cerion.stocklist.indicators.FunctionCall;
import org.cerion.stocklist.model.Function;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

class ChartFactory {

    private Context mContext;

    private static final int CHART_HEIGHT = 800;

    ChartFactory(Context context) {
        mContext = context;
    }

    Chart getChart(PriceList list, ChartParams params) {
        if(params.function.id == Function.VOLUME) {
            return getVolumeChart(list, params.overlays);
        }

        return getLineChart(list, params.function, params.overlays);
    }

    private class EntrySet {
        public EntrySet() {}
        public EntrySet(int color) {
            this.color = color;
        }

        List<Entry> entries = new ArrayList<>();
        int color = Color.BLACK;
    }

    private LineData getLineData(List<String> dates, ValueArray base, List<Overlay> overlays) {
        List<LineDataSet> sets = null;

        if(base instanceof MACDArray) { // Extends FloatArray so it needs to go above that
            EntrySet macd = new EntrySet();
            EntrySet signal = new EntrySet(Color.RED);
            EntrySet hist = new EntrySet(Color.BLUE);

            MACDArray arr = (MACDArray) base;
            for (int i = 0; i < base.size(); i++) {
                macd.entries.add(new Entry(arr.get(i), i));
                signal.entries.add(new Entry(arr.signal(i), i));
                hist.entries.add(new Entry(arr.hist(i), i));
            }

            sets = getLineDataSets(macd, signal, hist);
        } else if(base instanceof PairArray) {
            EntrySet pos = new EntrySet(Color.GREEN);
            EntrySet neg = new EntrySet(Color.RED);

            PairArray arr = (PairArray) base;
            for (int i = 0; i < base.size(); i++) {
                pos.entries.add(new Entry(arr.getPos(i), i));
                neg.entries.add(new Entry(arr.getNeg(i), i));
            }

            sets = getLineDataSets(pos, neg);

        } else if(base instanceof FloatArray) {
            EntrySet es = new EntrySet();
            FloatArray arr = (FloatArray)base;
            for (int i = 0; i < base.size(); i++) {
                es.entries.add(new Entry(arr.get(i), i));
            }
            sets = getLineDataSets(es);

        } else if(base instanceof BandArray) {
            EntrySet upper = new EntrySet(Color.RED);
            EntrySet lower = new EntrySet(Color.RED);
            EntrySet mid = new EntrySet();

            BandArray arr = (BandArray) base;
            for (int i = 0; i < base.size(); i++) {
                upper.entries.add(new Entry(arr.upper(i), i));
                lower.entries.add(new Entry(arr.lower(i), i));
                mid.entries.add(new Entry(arr.source(i), i));
            }

            sets = getLineDataSets(upper, lower, mid);
        }

        // Should only apply to FloatArray or VolumeArray
        if(overlays != null) {
            for (Overlay overlay : overlays) {
                sets.addAll(overlay.getDataSets(base));
            }
        }

        return new LineData(dates, sets);
    }

    private List<LineDataSet> getLineDataSets(EntrySet ...entrySets) {
        List<LineDataSet> sets = new ArrayList<>();

        for(EntrySet entrySet : entrySets) {
            LineDataSet dataSet = new LineDataSet(entrySet.entries, ""); // Ignore label, its set later
            dataSet.setDrawCircles(false);
            dataSet.setDrawValues(false);
            dataSet.setColor(entrySet.color);

            sets.add(dataSet);
        }

        return sets;
    }

    private Chart getLineChart(PriceList list, FunctionCall functionCall, List<Overlay> overlays) {
        ValueArray arr = functionCall.eval(list);

        LineChart chart = new LineChart(mContext);
        chart.setMinimumHeight(CHART_HEIGHT);

        LineData lineData = getLineData(getDates(list), arr, overlays);

        chart.setData(lineData);

        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        chart.setDescription("");

        int size = (overlays != null ? overlays.size() + 1 : 1);

        // Set labels so multi-line sets are not duplicated on legend
        Legend l = chart.getLegend();
        int[] colors = new int[size];
        String[] labels = new String[size];
        colors[0] = Color.BLACK;
        labels[0] = getLabel(functionCall);

        if(overlays != null) {
            for (int i = 1; i <= overlays.size(); i++) {
                Overlay o = overlays.get(i - 1);
                labels[i] = o.getLabel();
                colors[i] = o.getColor();
            }
        }

        l.setCustom(colors, labels);

        return chart;
    }

    private Chart getVolumeChart(PriceList list, List<Overlay> overlays) {
        VolumeArray base = list.getVolume();

        CombinedChart chart = new CombinedChart(mContext);
        chart.setMinimumHeight(ChartFactory.CHART_HEIGHT);

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            barEntries.add(new BarEntry(list.volume(i), i));
        }

        BarDataSet dataSet = new BarDataSet(barEntries, "Volume");
        dataSet.setDrawValues(false);
        BarData barData = new BarData(getDates(list),dataSet);

        ArrayList<LineDataSet> sets = new ArrayList<>();
        //sets.addAll( Overlay.getSMA(20).getDataSets(list.getVolume()) );
        // Should only apply to FloatArray or VolumeArray
        if(overlays != null) {
            for (Overlay overlay : overlays) {
                sets.addAll(overlay.getDataSets(base));
            }
        }

        LineData lineData = new LineData(getDates(list), sets);


        CombinedData data = new CombinedData(getDates(list));
        data.setData(barData);
        data.setData(lineData);

        //chart.setData(new BarData(getDates(list), dataSet));
        chart.setData(data);
        chart.setDescription("");

        //Set Y axis
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        chart.getAxisRight().setLabelCount(3, false);

        return chart;
    }

    private static String getLabel(FunctionCall call) {
        return call.id.toString() + " " + TextUtils.join(",", call.params);
    }

    public static List<String> getDates(PriceList list) {

        DateFormat mDateFormat = new SimpleDateFormat("MMM d, yy");

        List<String> dates = new ArrayList<>();
        for (Price p : list)
            dates.add(mDateFormat.format(p.date));

        return dates;
    }

}
