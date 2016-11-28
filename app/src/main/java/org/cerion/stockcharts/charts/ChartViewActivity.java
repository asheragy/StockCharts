package org.cerion.stockcharts.charts;


import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.database.StockDB;
import org.cerion.stockcharts.database.StockDataManager;
import org.cerion.stockcharts.model.HistoricalDates;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.arrays.VolumeArray;
import org.cerion.stocklist.model.FunctionDef;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.model.Overlay;

public class ChartViewActivity extends AppCompatActivity
        implements
        OverlaysDialogFragment.OnSelectListener, // TODO remove
        ChartHolder.OnDataRequestListener
{

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

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddChart();
            }
        });
    }

    private void onAddChart() {
        //DialogFragment newFragment = IndicatorsDialogFragment.newInstance(R.string.indicators);
        //newFragment.show(getFragmentManager(),"dialog");

        final ChartHolder holder = new ChartHolder(this, mSymbol);

        holder.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCharts.removeView(holder);
            }
        });

        // TODO do this inside holder
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
    public void select(OverlayDataSet overlay) {
        FunctionDef def = mLastActiveChart.mChartParams.function.id.getDef();

        if(def.result == VolumeArray.class)
        {
            if(overlay.getType() == Overlay.BB || overlay.getType() == Overlay.KAMA) {
                Log.d(TAG,"unsupported overlay on Volume: " + overlay.getLabel());
                return;
            }
        }

        mLastActiveChart.addOverlay(overlay);
    }

    private void onAddOverlay() {
        DialogFragment newFragment = OverlaysDialogFragment.newInstance(R.string.overlays);
        newFragment.show(getFragmentManager(),"dialog");
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
