package org.cerion.stockcharts.charts;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.database.StockDB;
import org.cerion.stocklist.Enums;
import org.cerion.stocklist.Price;
import org.cerion.stocklist.PriceList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChartTestActivity extends AppCompatActivity {


    LinearLayout mCharts;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_test_activity);

        mCharts = (LinearLayout) findViewById(R.id.charts);

        StockDB db = StockDB.getInstance(this);
        PriceList list = db.getPriceList("AAPL", Enums.Interval.MONTHLY);

        mCharts.addView(getPriceChart(list));
        mCharts.addView(getVolumeChart(list));
        mCharts.addView(getSMAChart(list));
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ChartTest Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://org.cerion.stockcharts/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ChartTest Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://org.cerion.stockcharts/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
