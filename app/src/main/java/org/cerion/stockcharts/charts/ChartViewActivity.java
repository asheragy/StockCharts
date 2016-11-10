package org.cerion.stockcharts.charts;


import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.database.StockDataManager;
import org.cerion.stockcharts.database.StockDB;
import org.cerion.stockcharts.model.HistoricalDates;
import org.cerion.stocklist.Price;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.model.Function;
import org.cerion.stocklist.model.Interval;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChartViewActivity extends AppCompatActivity
        implements IndicatorsDialogFragment.OnSelectListener, OverlaysDialogFragment.OnSelectListener, ChartHolder.OnDataRequestListener {

    private static final String TAG = ChartViewActivity.class.getSimpleName();
    public static final String EXTRA_SYMBOL = "symbol";

    private LinearLayout mCharts;
    private String mSymbol;
    private ChartHolder mLastActiveChart;
    private static final Interval INTERVAL = Interval.DAILY;
    private StockDataManager mDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_view_activity);

        mSymbol = getIntent().getStringExtra(EXTRA_SYMBOL);
        mCharts = (LinearLayout) findViewById(R.id.charts);
        mDataManager = new StockDataManager(this);

        StockDB db = StockDB.getInstance(this);
        HistoricalDates dates = db.getHistoricalDates(mSymbol, INTERVAL);

        if(dates == null) {
            GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
                @Override
                public void run() {
                    StockDataManager du = new StockDataManager(ChartViewActivity.this);
                    du.updatePrices(mSymbol, INTERVAL);
                }

                @Override
                public void onFinish() {
                    Log.d(TAG,"Updated prices for " + mSymbol);
                }
            });

            task.execute();
        }

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
    public void select(Function id) {
        final ChartHolder holder = new ChartHolder(this, mSymbol, id);

        //params.overlays.add(Overlay.getBB(20,2.0f));
        //Chart chart = ChartFactory.getLineChart(this, mList, call, null);

        holder.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCharts.removeView(holder);
            }
        });

        holder.findViewById(R.id.add_overlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLastActiveChart = holder;
                onAddOverlay();
            }
        });

        mCharts.addView(holder);
    }

    @Override
    public void select(Overlay overlay) {
        mLastActiveChart.addOverlay(overlay);
    }

    private void onAddOverlay() {
        DialogFragment newFragment = OverlaysDialogFragment.newInstance(R.string.overlays);
        newFragment.show(getFragmentManager(),"dialog");
    }

    public List<String> getDates(PriceList list) {

        DateFormat mDateFormat = new SimpleDateFormat("MMM d, yy");

        List<String> dates = new ArrayList<>();
        for (Price p : list)
            dates.add(mDateFormat.format(p.date));

        return dates;
    }

    @Override
    public void onRequest(final ChartHolder holder, final String symbol, final Interval interval) {

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            private PriceList result;
            @Override
            public void run() {
                result = mDataManager.getLatestPrices(symbol, interval);
            }

            @Override
            public void onFinish() {
                holder.loadChart(result);
            }
        });
        task.execute();

    }
}
