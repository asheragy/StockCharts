package org.cerion.stockcharts.charts;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import org.cerion.stockcharts.R;
import org.cerion.stocklist.model.Interval;

public class ChartViewActivity extends AppCompatActivity {
    private static final String TAG = ChartViewActivity.class.getSimpleName();
    public static final String EXTRA_SYMBOL = "symbol";

    private LinearLayout mCharts;
    private String mSymbol;
    private Interval mInterval = Interval.DAILY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_view);

        mSymbol = getIntent().getStringExtra(EXTRA_SYMBOL);
        mCharts = (LinearLayout) findViewById(R.id.charts);

        findViewById(R.id.add_price).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddPriceChart();
            }
        });

        findViewById(R.id.add_volume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddVolumeChart();
            }
        });

        findViewById(R.id.add_indicator).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddIndicatorChart();
            }
        });

        findViewById(R.id.daily).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectInterval(Interval.DAILY);
            }
        });

        findViewById(R.id.weekly).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectInterval(Interval.WEEKLY);
            }
        });

        findViewById(R.id.monthly).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectInterval(Interval.MONTHLY);
            }
        });

        //onAddPriceChart();
        //onAddVolumeChart();
    }

    private void onSelectInterval(Interval interval) {
        mInterval = interval;
        for(int i = 0; i < mCharts.getChildCount(); i++) {
            ChartHolderBase holder = (ChartHolderBase)mCharts.getChildAt(i);
            //onRequest(holder, mSymbol);
            holder.reload(interval);
        }
    }

    private void onAddPriceChart() {
        addHolder(new ChartHolderPrice(this, mSymbol, mInterval));
    }

    private void onAddVolumeChart() {
        addHolder(new ChartHolderVolume(this, mSymbol, mInterval));
    }

    private void onAddIndicatorChart() {
        addHolder(new ChartHolderIndicator(this, mSymbol, mInterval));
    }

    private void addHolder(final ChartHolderBase holder) {
        holder.setOnRemoveClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCharts.removeView(holder);
            }
        });

        mCharts.addView(holder);
    }

}
