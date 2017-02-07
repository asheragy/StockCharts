package org.cerion.stockcharts.charts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;

import com.github.mikephil.charting.charts.BarLineChartBase;
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
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.cerion.stockcharts.database.StockDataManager;
import org.cerion.stocklist.Price;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.arrays.BandArray;
import org.cerion.stocklist.arrays.FloatArray;
import org.cerion.stocklist.arrays.MACDArray;
import org.cerion.stocklist.arrays.PairArray;
import org.cerion.stocklist.arrays.ValueArray;
import org.cerion.stocklist.functions.FunctionCall;
import org.cerion.stocklist.functions.Indicator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class ChartFactory {

    private Context mContext;

    private static final int CHART_HEIGHT_PRICE = 800;
    private static final int CHART_HEIGHT = 400;
    private static DateFormat mDateFormat = new SimpleDateFormat("MMM d, yy");
    //private static DateFormat mDateFormatMonthly = new SimpleDateFormat("MMM 'yy");
    private Description mDesc = new Description();
    //private boolean mLogScale = false;
    private StockDataManager mDataManager;

    // TODO cache lists here, if the same one is requested multiple times hold it

    ChartFactory(Context context) {
        mContext = context;
        mDesc.setText("");
        mDataManager = new StockDataManager(mContext);
    }

    Chart getPriceChart(ChartParams.Price params) {
        // TODO check logscale and convert here
        PriceList list = mDataManager.getLatestPrices(params.symbol, params.interval);

        if(params.lineChart)
            return getPriceLineChart(list, params);

        return getCandleChart(list, params);
    }

    Chart getIndicatorChart(ChartParams.Indicator params) {
        //mLogScale = params.logscale;
        PriceList list = mDataManager.getLatestPrices(params.symbol, params.interval);
        return getLineChart(list, params);
    }

    Chart getVolumeChart(ChartParams params) {
        PriceList list = mDataManager.getLatestPrices(params.symbol, params.interval);
        return getVolumeChart(list, params);
    }

    Chart getEmptyChart() {
        Chart chart = new LineChart(mContext);
        chart.setDescription(mDesc);
        chart.setMinimumHeight(ChartFactory.CHART_HEIGHT);
        return chart;
    }

    private class EntrySet {
        EntrySet() {}
        EntrySet(int color) {
            this.color = color;
        }

        List<Entry> entries = new ArrayList<>();
        int color = Color.BLACK;
    }

    private Chart getCandleChart(PriceList list, ChartParams params) {
        CombinedChart chart = new CombinedChart(mContext);
        setChartDefaults(chart, list);
        chart.setMinimumHeight(ChartFactory.CHART_HEIGHT_PRICE);

        ArrayList<CandleEntry> entries = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Price p = list.get(i);
            if(params.logscale)
                entries.add(new CandleEntry(i, (float)Math.log(p.high+1), (float)Math.log(p.low+1), (float)Math.log(p.open+1), (float)Math.log(p.close+1))); // order is high, low, open, close
            else
                entries.add(new CandleEntry(i, p.high, p.low, p.open, p.close)); // order is high, low, open, close
        }

        CandleDataSet dataSet = new CandleDataSet(entries, "Price");
        dataSet.setDrawValues(false);
        dataSet.setDecreasingColor(Colors.CANDLE_DOWN);
        dataSet.setDecreasingPaintStyle(Paint.Style.FILL);
        dataSet.setIncreasingColor(Colors.CANDLE_UP);
        dataSet.setIncreasingPaintStyle(Paint.Style.FILL);
        CandleData candleData = new CandleData(dataSet);

        List<ILineDataSet> sets = OverlayDataSet.getLineDataSets(list, params.overlays);
        /*
        List<ILineDataSet> sets = new ArrayList<>();
        if(params.overlays != null) {
            FloatArray base = list.getClose();
            for (OverlayDataSet overlay : params.overlays) {
                sets.addAll(overlay.getDataSets(base));
            }
        }
        */

        LineData lineData = new LineData(sets);
        CombinedData data = new CombinedData();
        data.setData(candleData);
        data.setData(lineData);
        chart.setDrawOrder( new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE});
        chart.setData(data);

        setLegend(chart, params, "Price");

        return chart;
    }

    private Chart getPriceLineChart(PriceList list, ChartParams.Price params) {
        LineChart chart = new LineChart(mContext);
        setChartDefaults(chart, list);
        chart.setMinimumHeight(ChartFactory.CHART_HEIGHT_PRICE);

        LineData lineData = getLineData(list.getClose(), params.overlays);
        chart.setData(lineData);

        setLegend(chart, params, "Price");
        return chart;
    }

    private Chart getLineChart(PriceList list, ChartParams.Indicator params) {
        LineChart chart = new LineChart(mContext);
        setChartDefaults(chart, list);
        chart.setMinimumHeight(ChartFactory.CHART_HEIGHT);

        ValueArray arr = params.function.eval(list);
        LineData lineData = getLineData(arr, params.overlays);
        chart.setData(lineData);

        setLegend(chart, params, getLabel(params.function));

        return chart;
    }

    private Chart getVolumeChart(PriceList list, ChartParams params) {
        CombinedChart chart = new CombinedChart(mContext);
        setChartDefaults(chart, list);

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if(params.logscale)
                barEntries.add(new BarEntry(i, (float)Math.log(list.volume(i)) ));
            else
                barEntries.add(new BarEntry(i, list.volume(i)));
        }

        BarDataSet dataSet = new BarDataSet(barEntries, "Volume");
        dataSet.setDrawValues(false);
        BarData barData = new BarData(dataSet);


        // Should only apply to FloatArray or VolumeArray
        /*
        List<ILineDataSet> sets = new ArrayList<>();
        if(params.overlays != null) {
            FloatArray base = list.getVolume();
            for (OverlayDataSet overlay : params.overlays) {
                sets.addAll(overlay.getDataSets(base));
            }
        }
        */

        List<ILineDataSet> sets = OverlayDataSet.getLineDataSets(list.getVolume(), params.overlays);

        LineData lineData = new LineData(sets);
        CombinedData data = new CombinedData();
        data.setData(barData);
        data.setData(lineData);
        chart.setData(data);

        setLegend(chart, params, "Volume");

        return chart;
    }

    private void setChartDefaults(BarLineChartBase chart, PriceList list) {
        chart.setDescription(mDesc);
        chart.setMinimumHeight(ChartFactory.CHART_HEIGHT);

        //Set Y axis
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        chart.getAxisRight().setLabelCount(3, false);

        XAxis xaxis = chart.getXAxis();
        xaxis.setValueFormatter(getAxisFormatter(list));
    }

    private void setLegend(Chart chart, ChartParams params, String label) {
        List<OverlayDataSet> overlays = params.overlays;

        // Set labels so multi-line sets are not duplicated on legend
        int size = (overlays != null ? overlays.size() + 1 : 1);
        LegendEntry[] le = new LegendEntry[size];

        le[0] = new LegendEntry(label, Legend.LegendForm.DEFAULT, Float.NaN, Float.NaN, null, Color.BLACK);

        if(overlays != null) {
            for (int i = 1; i <= overlays.size(); i++) {
                OverlayDataSet o = overlays.get(i - 1);
                le[i] = new LegendEntry(o.getLabel(), Legend.LegendForm.DEFAULT, Float.NaN, Float.NaN, null, o.getColor());
            }
        }

        chart.getLegend().setCustom(le);
    }

    private LineData getLineData(ValueArray base, List<OverlayDataSet> overlays) {
        List<ILineDataSet> sets;

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

            /*
            EntrySet pos = new EntrySet(Color.GREEN);
            EntrySet neg = new EntrySet(Color.RED);

            PairArray arr = (PairArray) base;
            for (int i = 0; i < base.size(); i++) {
                pos.entries.add(new Entry(i, arr.getPos(i)));
                neg.entries.add(new Entry(i, arr.getNeg(i)));
            }

            sets = getLineDataSets(pos, neg);
            */

            sets = Tools.getPairDataSet((PairArray)base, Color.GREEN, Color.RED);

        }
        /* Top level chart won't be a band array
        else if(base instanceof BandArray) {
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
        } */
        else { // FloatArray
            EntrySet es = new EntrySet();
            FloatArray arr = (FloatArray)base;
            for (int i = 0; i < base.size(); i++) {
                es.entries.add(new Entry(i, arr.get(i)));
            }
            sets = getLineDataSets(es);

            // Only FloatArray can have overlays
            /*
            if(overlays != null) {
                for (OverlayDataSet overlay : overlays) {
                    sets.addAll(overlay.getDataSets(base));
                }
            }
            */
            sets.addAll( OverlayDataSet.getLineDataSets(arr, overlays));
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
        return ((Indicator)call.id).name() + " " + TextUtils.join(",", call.params);
    }

    private IAxisValueFormatter getAxisFormatter(PriceList list) {
        final Date[] dates = list.getDates();

        return new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int v = (int)value;
                return mDateFormat.format(dates[v]); // TODO format based on interval
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };
    }
}
