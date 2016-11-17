package org.cerion.stockcharts.charts;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

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
import java.util.Date;
import java.util.List;

class ChartFactory {

    private Context mContext;

    private static final int CHART_HEIGHT = 800;
    private static DateFormat mDateFormat = new SimpleDateFormat("MMM d, yy");
    private Description mDesc = new Description();

    ChartFactory(Context context) {
        mContext = context;
        mDesc.setText("");
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

    private Chart getLineChart(PriceList list, FunctionCall functionCall, List<Overlay> overlays) {
        ValueArray arr = functionCall.eval(list);
        LineData lineData = getLineData(arr, overlays);

        LineChart chart = new LineChart(mContext);
        chart.setMinimumHeight(CHART_HEIGHT);
        chart.setData(lineData);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        chart.setDescription(mDesc);

        XAxis xaxis = chart.getXAxis();
        xaxis.setValueFormatter(getAxisFormatter(list));

        // Set labels so multi-line sets are not duplicated on legend
        int size = (overlays != null ? overlays.size() + 1 : 1);
        LegendEntry[] le = new LegendEntry[size];
        le[0] = new LegendEntry(getLabel(functionCall), Legend.LegendForm.DEFAULT, Float.NaN, Float.NaN, null, Color.BLACK);

        if(overlays != null) {
            for (int i = 1; i <= overlays.size(); i++) {
                Overlay o = overlays.get(i - 1);
                le[i] = new LegendEntry(o.getLabel(), Legend.LegendForm.DEFAULT, Float.NaN, Float.NaN, null, o.getColor());
            }
        }

        chart.getLegend().setCustom(le);

        return chart;
    }

    private Chart getVolumeChart(PriceList list, List<Overlay> overlays) {
        VolumeArray base = list.getVolume();

        CombinedChart chart = new CombinedChart(mContext);
        chart.setDescription(mDesc);
        chart.setMinimumHeight(ChartFactory.CHART_HEIGHT);

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            barEntries.add(new BarEntry(i, list.volume(i)));
        }

        BarDataSet dataSet = new BarDataSet(barEntries, "Volume");
        dataSet.setDrawValues(false);
        BarData barData = new BarData(dataSet);

        List<ILineDataSet> sets = new ArrayList<>();

        // Should only apply to FloatArray or VolumeArray
        if(overlays != null) {
            for (Overlay overlay : overlays) {
                sets.addAll(overlay.getDataSets(base));
            }
        }

        LineData lineData = new LineData(sets);
        CombinedData data = new CombinedData();
        data.setData(barData);
        data.setData(lineData);
        chart.setData(data);

        //Set Y axis
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        chart.getAxisRight().setLabelCount(3, false);

        XAxis xaxis = chart.getXAxis();
        xaxis.setValueFormatter(getAxisFormatter(list));

        return chart;
    }

    private LineData getLineData(ValueArray base, List<Overlay> overlays) {
        List<ILineDataSet> sets = null;

        if(base instanceof MACDArray) { // Extends FloatArray so it needs to go above that
            EntrySet macd = new EntrySet();
            EntrySet signal = new EntrySet(Color.RED);
            EntrySet hist = new EntrySet(Color.BLUE);

            MACDArray arr = (MACDArray) base;
            for (int i = 0; i < base.size(); i++) {
                macd.entries.add(  new Entry(i, arr.get(i)));
                signal.entries.add(new Entry(i, arr.signal(i)));
                hist.entries.add(  new Entry(i, arr.hist(i)));
            }

            sets = getLineDataSets(macd, signal, hist);
        } else if(base instanceof PairArray) {
            EntrySet pos = new EntrySet(Color.GREEN);
            EntrySet neg = new EntrySet(Color.RED);

            PairArray arr = (PairArray) base;
            for (int i = 0; i < base.size(); i++) {
                pos.entries.add(new Entry(i, arr.getPos(i)));
                neg.entries.add(new Entry(i, arr.getNeg(i)));
            }

            sets = getLineDataSets(pos, neg);

        } else if(base instanceof FloatArray) {
            EntrySet es = new EntrySet();
            FloatArray arr = (FloatArray)base;
            for (int i = 0; i < base.size(); i++) {
                es.entries.add(new Entry(i, arr.get(i)));
            }
            sets = getLineDataSets(es);

        } else if(base instanceof BandArray) {
            EntrySet upper = new EntrySet(Color.RED);
            EntrySet lower = new EntrySet(Color.RED);
            EntrySet mid = new EntrySet();

            BandArray arr = (BandArray) base;
            for (int i = 0; i < base.size(); i++) {
                upper.entries.add(new Entry(i, arr.upper(i)));
                lower.entries.add(new Entry(i, arr.lower(i)));
                mid.entries.add(  new Entry(i, arr.source(i)));
            }

            sets = getLineDataSets(upper, lower, mid);
        }

        // Should only apply to FloatArray or VolumeArray
        if(overlays != null) {
            for (Overlay overlay : overlays) {
                sets.addAll(overlay.getDataSets(base));
            }
        }

        return new LineData(sets);
    }

    private List<ILineDataSet> getLineDataSets(EntrySet ...entrySets) {
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

    private static String getLabel(FunctionCall call) {
        return call.id.toString() + " " + TextUtils.join(",", call.params);
    }

    private IAxisValueFormatter getAxisFormatter(PriceList list) {
        final Date[] dates = list.getDates();

        return new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int v = (int)value;
                return mDateFormat.format(dates[v]);
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };
    }

    /*
    public static List<String> getDates(PriceList list) {

        DateFormat mDateFormat = new SimpleDateFormat("MMM d, yy");

        List<String> dates = new ArrayList<>();
        for (Price p : list)
            dates.add(mDateFormat.format(p.date));

        return dates;
    }
    */

}
