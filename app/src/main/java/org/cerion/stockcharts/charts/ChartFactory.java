package org.cerion.stockcharts.charts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

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
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.cerion.stockcharts.database.StockDataManager;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.charts.DataSet;
import org.cerion.stocklist.charts.IndicatorChart;
import org.cerion.stocklist.charts.PriceChart;
import org.cerion.stocklist.charts.StockChart;
import org.cerion.stocklist.charts.VolumeChart;
import org.cerion.stocklist.model.Interval;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class ChartFactory {

    private Context mContext;

    private static final int CHART_HEIGHT_PRICE = 800;
    private static final int CHART_HEIGHT = 400;
    private static DateFormat mDateFormat        = new SimpleDateFormat("MMM d, yy");
    private static DateFormat mDateFormatMonthly = new SimpleDateFormat("MMM ''yy");
    private Description mDesc = new Description();
    private StockDataManager mDataManager;

    // TODO cache lists here, if the same one is requested multiple times hold it

    ChartFactory(Context context) {
        mContext = context;
        mDesc.setText("");
        mDataManager = new StockDataManager(mContext);
    }

    Chart getChart(StockChart chart, String symbol) {
        PriceList list = mDataManager.getLatestPrices(symbol, chart.interval);
        chart.setPriceList(list);

        if(chart instanceof PriceChart)
            return getPriceChart((PriceChart)chart);

        if(chart instanceof IndicatorChart)
            return getLineChart((IndicatorChart)chart);

        return getVolumeChart((VolumeChart)chart);
    }

    Chart getEmptyChart() {
        Chart chart = new LineChart(mContext);
        chart.setDescription(mDesc);
        chart.setMinimumHeight(ChartFactory.CHART_HEIGHT);
        return chart;
    }

    @SuppressWarnings("unchecked")
    private Chart getPriceChart(PriceChart pchart) {
        BarLineChartBase chart;

        List<DataSet> sets = getDataSets(pchart);

        if(pchart.candleData) {
            chart = new CombinedChart(mContext);
            CombinedData data = new CombinedData();
            CandleData candleData = getCandleData(sets);
            data.setData(candleData);

            LineData lineData = getLineData(sets);
            data.setData(lineData);
            ((CombinedChart)chart).setDrawOrder(new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE});
            chart.setData(data);
        } else {
            chart = new LineChart(mContext);
            LineData lineData = getLineData(sets);
            chart.setData(lineData);
        }

        setChartDefaults(chart, pchart);
        chart.setMinimumHeight(ChartFactory.CHART_HEIGHT_PRICE);

        if(pchart.logScale) {
            YAxis axis = chart.getAxisRight();
            axis.setValueFormatter(getLogScaleYAxis());
        }

        setLegend(chart, sets);
        return chart;
    }

    private Chart getLineChart(IndicatorChart ichart) {
        LineChart chart = new LineChart(mContext);
        setChartDefaults(chart, ichart);
        chart.setMinimumHeight(ChartFactory.CHART_HEIGHT);

        List<DataSet> sets = getDataSets(ichart);
        chart.setData(getLineData(sets));

        setLegend(chart, sets);
        return chart;
    }

    private Chart getVolumeChart(VolumeChart vchart) {
        CombinedChart chart = new CombinedChart(mContext);
        setChartDefaults(chart, vchart);

        if(vchart.logScale) {
            YAxis axis = chart.getAxisRight();
            axis.setValueFormatter(getLogScaleYAxis());
        }

        List<DataSet> dataSets = getDataSets(vchart);
        CombinedData data = new CombinedData();
        data.setData(getBarData(dataSets));
        data.setData(getLineData(dataSets));
        chart.setData(data);

        setLegend(chart, dataSets);
        return chart;
    }

    private void setChartDefaults(BarLineChartBase chart, StockChart stockchart) {
        chart.setDescription(mDesc);
        chart.setMinimumHeight(ChartFactory.CHART_HEIGHT);

        //Set Y axis
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        chart.getAxisRight().setLabelCount(3, false);

        XAxis xaxis = chart.getXAxis();
        xaxis.setValueFormatter(getAxisFormatter(stockchart.getDates(), stockchart.interval));
    }

    private void setLegend(Chart chart, List<DataSet> sets) {
        List<LegendEntry> entries = new ArrayList<>();

        String lastLabel = "";
        int lastColor = -1;
        for(DataSet set : sets) {
            String label = set.getLabel();
            int color = set.getColor();
            LegendEntry entry = null;

            if(lastLabel.contentEquals(label)) {
                if(lastColor != color) {
                    entry = new LegendEntry(label, Legend.LegendForm.DEFAULT, Float.NaN, Float.NaN, null, color);
                    entries.get(entries.size() - 1).label = null; // label needs to go on the last one added
                }
            } else {
                entry = new LegendEntry(label, Legend.LegendForm.DEFAULT, Float.NaN, Float.NaN, null, color);
            }

            if(entry != null)
                entries.add(entry);

            lastLabel = label;
            lastColor = color;
        }

        chart.getLegend().setCustom(entries);
    }

    private List<DataSet> getDataSets(StockChart chart) {
        chart.setPrimaryColors(Colors.CHART_PRIMARY);
        chart.setSecondaryColors(Colors.CHART_SECONDARY);
        return chart.getDataSets();
    }

    private BarData getBarData(List<DataSet> sets) {
        List<IBarDataSet> result = new ArrayList<>();

        for(DataSet set : sets) {
            if(set.getLineType() == DataSet.LineType.BAR) {
                ArrayList<BarEntry> entries = new ArrayList<>();
                for (int i = 0; i < set.size(); i++)
                    entries.add(new BarEntry(i, set.get(i)));

                BarDataSet dataSet = new BarDataSet(entries, set.getLabel());
                dataSet.setDrawValues(false);
                result.add(dataSet);
            }
        }

        return new BarData(result);
    }

    private LineData getLineData(List<DataSet> sets) {
        List<ILineDataSet> result = new ArrayList<>();

        for(DataSet set : sets) {
            if(set.getLineType() == DataSet.LineType.LINE || set.getLineType() == DataSet.LineType.DOTTED) {
                ArrayList<Entry> entries = new ArrayList<>();
                for (int i = 0; i < set.size(); i++) {
                    entries.add(new Entry(i, set.get(i)));
                }

                LineDataSet lineDataSet = new LineDataSet(entries, set.getLabel());
                lineDataSet.setDrawCircles(false);
                lineDataSet.setDrawValues(false);
                lineDataSet.setColor(set.getColor());

                if(set.getLineType() == DataSet.LineType.DOTTED) {
                    // https://github.com/PhilJay/MPAndroidChart/pull/2622
                    // This should be Transparent but using White because of bug
                    lineDataSet.setColor(Color.rgb(250,250,250));
                    lineDataSet.setDrawCircles(true);
                    lineDataSet.setCircleRadius(1f);
                    lineDataSet.setCircleColor(set.getColor());
                }

                result.add(lineDataSet);
            }
        }

        return new LineData(result);
    }

    private CandleData getCandleData(List<DataSet> sets) {
        for(DataSet set : sets) {
            if(set.getLineType() == DataSet.LineType.CANDLE) {
                ArrayList<CandleEntry> entries = new ArrayList<>();
                org.cerion.stocklist.charts.CandleDataSet cds = (org.cerion.stocklist.charts.CandleDataSet)set;

                for (int i = 0; i < set.size(); i++) {
                    entries.add(new CandleEntry(i, cds.getHigh(i), cds.getLow(i), cds.getOpen(i), cds.getClose(i))); // order is high, low, open, close
                }

                CandleDataSet dataSet = new CandleDataSet(entries, set.getLabel());
                dataSet.setDrawValues(false);
                dataSet.setDecreasingColor(Colors.CANDLE_DOWN);
                dataSet.setDecreasingPaintStyle(Paint.Style.FILL);
                dataSet.setIncreasingColor(Colors.CANDLE_UP);
                dataSet.setIncreasingPaintStyle(Paint.Style.FILL);

                return new CandleData((dataSet));
            }
        }

        return null;
    }

    private IAxisValueFormatter getAxisFormatter(final Date[] dates, final Interval interval) {
        return new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int v = (int)value;
                if(interval == Interval.MONTHLY)
                    return mDateFormatMonthly.format(dates[v]);

                return mDateFormat.format(dates[v]);
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };
    }

    private IAxisValueFormatter getLogScaleYAxis() {

        return new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                // Round to 2 significant figures
                double actual = Math.exp(value);
                BigDecimal bd = new BigDecimal(actual);
                bd = bd.round(new MathContext(2));
                return bd.toPlainString();
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };
    }
}
