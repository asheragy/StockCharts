package org.cerion.stockcharts.charts;


import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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
import org.cerion.stockcharts.database.StockDB;
import org.cerion.stocklist.Enums;
import org.cerion.stocklist.Function;
import org.cerion.stocklist.Price;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.model.FunctionCall;
import org.cerion.stocklist.model.FunctionDef;
import org.cerion.stocklist.model.FunctionId;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChartViewActivity extends AppCompatActivity implements IndicatorsDialogFragment.OnSelectListener {

    LinearLayout mCharts;
    private PriceList mList;
    public static final String EXTRA_SYMBOL = "symbol";
    private String mSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_test_activity);

        mSymbol = getIntent().getStringExtra(EXTRA_SYMBOL);
        mCharts = (LinearLayout) findViewById(R.id.charts);

        StockDB db = StockDB.getInstance(this);
        mList = db.getPriceList(mSymbol, Enums.Interval.MONTHLY);

        //TODO, fix chartlist bug for 0 entries on overlays

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

    @Override
    public void select(FunctionId id) {

        FunctionCall call = null;

        if(id != null) {
            FunctionDef def = Function.getDef(id);
            call = new FunctionCall(id, def.default_values);
        }

        Chart chart = Charts.getLineChart(this, mList, null, call);
        mCharts.addView(chart);
    }

    public Chart getSMAChart(PriceList list) {

        List<Overlay> overlays = new ArrayList<>();
        overlays.add(Overlay.getEMA(20));
        overlays.add(Overlay.getSMA(100));
        overlays.add(Overlay.getBB(20, 2.0f));

        return Charts.getLineChart(this, list.getClose(), getDates(list), "Price", overlays);
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
        chart.setMinimumHeight(Charts.CHART_HEIGHT);

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
        chart.setMinimumHeight(Charts.CHART_HEIGHT);

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
