package org.cerion.stockcharts.charts;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.cerion.stocklist.Function;
import org.cerion.stocklist.Price;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.data.FloatArray;
import org.cerion.stocklist.data.ValueArray;
import org.cerion.stocklist.data.VolumeArray;
import org.cerion.stocklist.model.FunctionCall;
import org.cerion.stocklist.model.FunctionDef;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/*
CHART
-List of 1 or more functions on a PriceList
-Each function has options
    Line color [Edit color]
    Overlay(s) if single line [Add/Remove]
 */
public class ChartHelper {

    //TODO make this a non static class, takes context and pricelist as constructor, can get charts from passing Function + overlays
    public static final int CHART_HEIGHT = 800;


    /*
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
    */

    public static Chart getLineChart(Context context, PriceList list, ChartParams params) {
        FunctionCall functionCall = params.function;
        List<Overlay> overlays = params.overlays;

        //TODO, null function call just uses closing price
        FloatArray base;

        if(functionCall == null) {
            base = list.getClose();
        } else {
            ValueArray arr = Function.eval(list, functionCall);
            //FloatArray base = list.getClose();
            base = (FloatArray) arr;
        }

        List<String> dates = getDates(list);
        //FunctionDef def = Function.getDef(functionCall.id);
        //------------

        LineChart chart = new LineChart(context);
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

        ///-------- overlay start
        //sets.add(getOverlay(base));
        //TODO for bollingerbands 2 sets are added
        //--------- end

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
