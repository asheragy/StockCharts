package org.cerion.stockcharts.charts;

import android.content.Context;
import android.graphics.Color;
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
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.cerion.stockcharts.database.StockDataManager;
import org.cerion.stocklist.PriceList;
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
        return getPriceChart(list, params);
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

    private Chart getPriceChart(PriceList list, ChartParams.Price params) {
        BarLineChartBase chart;

        if(params.lineChart) {
            chart = new LineChart(mContext);
            LineData lineData = getLineData(list.getClose(), params.overlays);
            chart.setData(lineData);

        } else {
            chart = new CombinedChart(mContext);
            CombinedData data = new CombinedData();
            ICandleDataSet dataSet = DataSetConverter.getCandleDataSet(list, params.logscale);
            CandleData candleData = new CandleData(dataSet);
            data.setData(candleData);

            List<ILineDataSet> sets = OverlayDataSet.getLineDataSets(list, params.overlays);
            LineData lineData = new LineData(sets);
            data.setData(lineData);

            ((CombinedChart)chart).setDrawOrder(new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE});
            chart.setData(data);
        }

        setChartDefaults(chart, list);
        chart.setMinimumHeight(ChartFactory.CHART_HEIGHT_PRICE);

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
            sets = DataSetConverter.getMACDDataSet((MACDArray)base, Color.BLACK, Color.RED, Color.BLUE);
        } else if(base instanceof PairArray) {
            sets = DataSetConverter.getPairDataSet((PairArray)base, Color.GREEN, Color.RED);
        } else { // FloatArray
            FloatArray arr = (FloatArray)base;
            sets = DataSetConverter.getSingleDataSet(arr, Color.BLACK);

            // Add overlays
            sets.addAll( OverlayDataSet.getLineDataSets(arr, overlays));
        }

        return new LineData(sets);
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
