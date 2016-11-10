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
import org.cerion.stocklist.arrays.FloatArray;
import org.cerion.stocklist.arrays.ValueArray;
import org.cerion.stocklist.indicators.FunctionCall;
import org.cerion.stocklist.model.Function;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

class ChartFactory {

    private Context mContext;

    public static final int CHART_HEIGHT = 800;

    public ChartFactory(Context context) {
        mContext = context;
    }

    public Chart getChart(PriceList list, ChartParams params) {

        //TODO, this can probably evaluate pricelist and pass the base array + overlays

        //if(params.function != null && params.function.id == FunctionId.SMA_VOLUME) {
        //    return getVolumeChart(list, params);
        //}

        return getLineChart(list, params);
    }

    private Chart getLineChart(PriceList list, ChartParams params) {
        FunctionCall functionCall = params.function;
        List<Overlay> overlays = params.overlays;

        // TODO add function for this that just gets the closing price so we don't have to deal with null
        FloatArray base;

        if(functionCall == null) {
            base = list.getClose();
        } else {
            ValueArray arr = functionCall.eval(list);
            //FloatArray base = list.getClose();
            base = (FloatArray) arr;
        }

        List<String> dates = getDates(list);
        //FunctionDef def = Function.getDef(functionCall.id);
        //------------

        LineChart chart = new LineChart(mContext);
        chart.setMinimumHeight(CHART_HEIGHT);

        ArrayList<Entry> entries = new ArrayList<>();
        for(int i = 0; i < base.size(); i++) {
            entries.add(new Entry(base.get(i), i));
        }

        LineDataSet dataSet = new LineDataSet(entries, getLabel(functionCall));
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setColor(Color.BLACK);

        ArrayList<LineDataSet> sets = new ArrayList<>();
        sets.add(dataSet);

        if(overlays != null) {
            for (Overlay overlay : overlays) {
                sets.addAll(overlay.getDataSets(base));
            }
        }

        LineData lineData = new LineData(dates, sets);
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

    private Chart getVolumeChart(PriceList list, ChartParams params) {


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
        sets.addAll( Overlay.getSMA(20).getDataSets(list.getVolume()) );

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

/*
    public Chart getVolumeChart(PriceList list) {

        CombinedChart chart = new CombinedChart(this);
        chart.setMinimumHeight(ChartFactory.CHART_HEIGHT);

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            barEntries.add(new BarEntry(list.volume(i), i));
        }

        BarDataSet dataSet = new BarDataSet(barEntries, "Volume");
        dataSet.setDrawValues(false);
        BarData barData = new BarData(getDates(list),dataSet);

        ArrayList<LineDataSet> sets = new ArrayList<>();
        sets.addAll( Overlay.getSMA(20).getDataSets(list.getVolume()) );

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
*/

    private static String getLabel(FunctionCall call) {
        if(call == null)
            return "Price";

        return call.id.toString() + " " + TextUtils.join(",", call.params);
    }
/*
    public List<LineDataSet> getDataSets(ValueArray arr)
    {
        mValues = arr;
        if(mType == TYPE_BB)
            return getMultiDataSet();

        List<LineDataSet> sets = new ArrayList<>();
        sets.add( getSingleDataSet() );
        return sets;
    }


    */

    public static List<String> getDates(PriceList list) {

        DateFormat mDateFormat = new SimpleDateFormat("MMM d, yy");

        List<String> dates = new ArrayList<>();
        for (Price p : list)
            dates.add(mDateFormat.format(p.date));

        return dates;
    }

}
