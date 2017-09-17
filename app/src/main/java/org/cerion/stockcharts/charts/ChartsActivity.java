package org.cerion.stockcharts.charts;


import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.charts.views.ChartView;
import org.cerion.stockcharts.databinding.ActivityChartsBinding;
import org.cerion.stockcharts.ui.ViewModelActivity;
import org.cerion.stocklist.charts.IndicatorChart;
import org.cerion.stocklist.charts.PriceChart;
import org.cerion.stocklist.charts.StockChart;
import org.cerion.stocklist.charts.VolumeChart;
import org.cerion.stocklist.indicators.MACD;
import org.cerion.stocklist.model.Interval;

public class ChartsActivity extends ViewModelActivity<ChartsViewModel> {
    private static final String TAG = ChartsActivity.class.getSimpleName();
    private static final String EXTRA_SYMBOL = "symbol";
    private static final String STATE_CHARTS = "charts";
    private static final String STATE_INTERVAL = "interval";

    private LinearLayout mCharts;

    public static Intent newIntent(Context context, String symbol) {
        Intent intent = new Intent(context,ChartsActivity.class);
        intent.putExtra(ChartsActivity.EXTRA_SYMBOL, symbol);
        return intent;
    }

    @Override
    protected ChartsViewModel newViewModel() {
        return new ChartsViewModel(getIntent().getStringExtra(EXTRA_SYMBOL));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityChartsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_charts);
        binding.setViewmodel(getViewModel());

        //setContentView(R.layout.activity_charts);


        mCharts = (LinearLayout) findViewById(R.id.charts);
        //onSelectInterval(Interval.DAILY);

        findViewById(R.id.add_price).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddChart(new PriceChart());
            }
        });

        findViewById(R.id.add_volume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddChart(new VolumeChart());
            }
        });

        findViewById(R.id.add_indicator).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddChart(new IndicatorChart(new MACD()));
            }
        });

        findViewById(R.id.daily).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getViewModel().interval.set(Interval.DAILY);
            }
        });

        findViewById(R.id.weekly).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getViewModel().interval.set(Interval.WEEKLY);
            }
        });

        findViewById(R.id.monthly).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getViewModel().interval.set(Interval.MONTHLY);
            }
        });

        /*
        if (savedInstanceState != null) {
            mInterval = Interval.values()[savedInstanceState.getInt(STATE_INTERVAL)];
            ArrayList<ChartState> charts = (ArrayList<ChartState>)savedInstanceState.getSerializable(STATE_CHARTS);

            for(ChartState state : charts) {
                if(state.type == 0) {

                    PriceChart chart = new PriceChart();
                    chart.interval = mInterval;
                    addHolder(new ChartHolderPrice(this, mSymbol, chart));
                } else if(state.type == 1) {

                    VolumeChart chart = new VolumeChart();
                    chart.interval = mInterval;
                    addHolder(new ChartHolderVolume(this, mSymbol, chart));
                } else {
                    Indicator indicator = Indicator.values()[state.primaryValue];
                    IndicatorChart chart = new IndicatorChart(indicator.getInstance());
                    chart.interval = mInterval;
                    addHolder(new ChartHolderIndicator(this, mSymbol, chart));
                }
            }
            Log.d(TAG, "found chart");

        }
        else {
            //onAddPriceChart();
            //onAddVolumeChart();
            onAddIndicatorChart();
            //onAddIndicatorChart();
        }
        */

        // Restore previous charts
        if (IsRetained()) {
            for(ChartViewModel vm : getViewModel().charts) {
                ChartView view = new ChartView(this, vm);
                mCharts.addView(view);
            }
        } else {
            onAddChart(new PriceChart());
        }
    }

    private void onAddChart(StockChart chart) {
        ChartViewModel vm = new ChartViewModel(getViewModel(), chart);

        // chart.interval = viewModel.interval.get();
        ChartView view = new ChartView(this, vm);

        mCharts.addView(view);
        getViewModel().charts.add(vm);

        if (chart instanceof IndicatorChart)
            view.edit();
    }

    /*
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<ChartState> charts = new ArrayList<>();
        for(int i = 0; i < mCharts.getChildCount(); i++) {
            ChartView holder = (ChartView)mCharts.getChildAt(i);
            StockChart chart = holder.getStockChart();
            ChartState state = new ChartState(chart);
            charts.add(state);
        }

        outState.putSerializable(STATE_CHARTS, charts);
        outState.putInt(STATE_INTERVAL, viewModel.getInterval().ordinal());
    }
    */

    /*
    private class ChartState implements Serializable {
        private int type;
        private int primaryValue;

        public ChartState(StockChart chart) {
            if (chart instanceof PriceChart)
                type = 0;
            else if (chart instanceof VolumeChart)
                type = 1;
            else if (chart instanceof IndicatorChart) {
                type = 2;

                IndicatorChart ichart = (IndicatorChart)chart;
                primaryValue = ichart.getId().ordinal();
            }
        }
    }
    */
}
