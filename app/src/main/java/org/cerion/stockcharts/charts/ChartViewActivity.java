package org.cerion.stockcharts.charts;


import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.database.DatabaseUpdater;
import org.cerion.stockcharts.database.StockDB;
import org.cerion.stockcharts.model.HistoricalDates;
import org.cerion.stocklist.Enums;
import org.cerion.stocklist.Function;
import org.cerion.stocklist.Price;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.data.FloatArray;
import org.cerion.stocklist.model.FunctionCall;
import org.cerion.stocklist.model.FunctionDef;
import org.cerion.stocklist.model.FunctionId;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChartViewActivity extends AppCompatActivity implements IndicatorsDialogFragment.OnSelectListener, OverlaysDialogFragment.OnSelectListener {

    private static final String TAG = ChartViewActivity.class.getSimpleName();
    LinearLayout mCharts;
    private PriceList mList;
    public static final String EXTRA_SYMBOL = "symbol";
    private String mSymbol;
    private ViewGroup mLastActiveChart;

    //TODO make its own class and pass to chartHelper
    private class ChartParams {
        FunctionCall function;
        List<Overlay> overlays = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_test_activity);

        mSymbol = getIntent().getStringExtra(EXTRA_SYMBOL);
        mCharts = (LinearLayout) findViewById(R.id.charts);

        StockDB db = StockDB.getInstance(this);
        HistoricalDates dates = db.getHistoricalDates(mSymbol, Enums.Interval.MONTHLY);

        if(dates == null) {
            GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
                @Override
                public void run() {
                    DatabaseUpdater du = new DatabaseUpdater(ChartViewActivity.this);
                    du.updatePrices(mSymbol, Enums.Interval.MONTHLY);
                }

                @Override
                public void onFinish() {
                    Log.d(TAG,"Updated prices for " + mSymbol);
                }
            });

            task.execute();
        }

        //mList = db.getPriceList(mSymbol, Enums.Interval.MONTHLY);



        //mCharts.addView(getPriceChart(mList));
        //mCharts.addView(getVolumeChart(list));
        //mCharts.addView(getSMAChart(mList));

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddChart();
            }
        });
    }

    private void onAddChart() {
        DialogFragment newFragment = IndicatorsDialogFragment.newInstance(R.string.indicators);
        newFragment.show(getFragmentManager(),"dialog");
    }

    private void onAddOverlay() {
        DialogFragment newFragment = OverlaysDialogFragment.newInstance(R.string.overlays);
        newFragment.show(getFragmentManager(),"dialog");
    }

    @Override
    public void select(FunctionId id) {

        FunctionDef def = null;
        FunctionCall call = null;

        if(id != null) {
            def = Function.getDef(id);
            call = new FunctionCall(id, def.default_values);
        }

        // TODO async
        if(mList == null) {
            mList = StockDB.getInstance(this).getPriceList(mSymbol, Enums.Interval.MONTHLY);
        }

        ChartParams params = new ChartParams();
        params.function = call;

        //List<Overlay> overlays = new ArrayList<>();
        //overlays.add(Overlay.getEMA(17));
        Chart chart = ChartHelper.getLineChart(this, mList, params.overlays, call);

        final View holder = getLayoutInflater().inflate(R.layout.chart_holder, null);
        holder.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCharts.removeView(holder);
            }
        });

        //TODO see if holder can be its own class to automatically handle this stuff
        holder.findViewById(R.id.add_overlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLastActiveChart = (ViewGroup)holder;
                onAddOverlay();
            }
        });

        // If overlay is not allowed then hide it
        if(def != null && def.result != FloatArray.class) {
            holder.findViewById(R.id.add_overlay).setVisibility(View.GONE);
        }

        holder.setTag(params);

        FrameLayout frame = (FrameLayout)holder.findViewById(R.id.chart_frame);
        frame.addView(chart);

        mCharts.addView(holder);
    }

    @Override
    public void select(Overlay overlay) {

        ChartParams params = (ChartParams)mLastActiveChart.getTag();
        params.overlays.add(overlay);

        FrameLayout frame = (FrameLayout)mLastActiveChart.findViewById(R.id.chart_frame);

        Chart chart = ChartHelper.getLineChart(this, mList, params.overlays, params.function);
        frame.addView(chart);
    }

    public Chart getSMAChart(PriceList list) {

        List<Overlay> overlays = new ArrayList<>();
        overlays.add(Overlay.getEMA(20));
        overlays.add(Overlay.getSMA(100));
        overlays.add(Overlay.getBB(20, 2.0f));

        return null;//ChartHelper.getLineChart(this, list.getClose(), getDates(list), "Price", overlays);
    }

/*
    public Chart getSMAChart(PriceList list) {

        LineChart chart = new LineChart(this);
        chart.setMinimumHeight(CHART_HEIGHT);

        ArrayList<Entry> entries = new ArrayList<>();
        int pos = 0;
        for (Price p : list) {
            entries.add(new Entry(p.close, pos));
            pos++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Price");
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);

        ArrayList<LineDataSet> sets = new ArrayList<>();
        sets.add(dataSet);

        sets.addAll(Overlay.getEMA(20).getDataSets(list.getClose()));
        sets.addAll(Overlay.getSMA(100).getDataSets(list.getClose()));
        sets.addAll(Overlay.getBB(20, 2.0f).getDataSets(list.getClose()));

        LineData lineData = new LineData(getDates(list), sets);
        chart.setData(lineData);

        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        chart.setDescription("");

        //LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //llp.setMargins(0, 50, 0, 0); // llp.setMargins(left, top, right, bottom);
        //chart.setLayoutParams(llp);

        return chart;
    }
    */


    public Chart getVolumeChart(PriceList list) {

        CombinedChart chart = new CombinedChart(this);
        chart.setMinimumHeight(ChartHelper.CHART_HEIGHT);

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

    public Chart getPriceChart(PriceList list) {

        LineChart chart = new LineChart(this);
        chart.setMinimumHeight(ChartHelper.CHART_HEIGHT);

        ArrayList<Entry> entries = new ArrayList<>();
        int pos = 0;
        for (Price p : list) {
            entries.add(new Entry(p.close, pos));
            pos++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Price");
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(getDates(list), dataSet);
        chart.setData(lineData);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        chart.setDescription("");

        return chart;
    }

    public List<String> getDates(PriceList list) {

        DateFormat mDateFormat = new SimpleDateFormat("MMM d, yy");

        List<String> dates = new ArrayList<>();
        for (Price p : list)
            dates.add(mDateFormat.format(p.date));

        return dates;
    }
}
