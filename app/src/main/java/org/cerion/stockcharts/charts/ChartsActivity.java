package org.cerion.stockcharts.charts;


import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.edmodo.rangebar.RangeBar;

import org.cerion.stockcharts.Injection;
import org.cerion.stockcharts.R;
import org.cerion.stockcharts.charts.views.ChartView;
import org.cerion.stockcharts.databinding.ActivityChartsBinding;
import org.cerion.stockcharts.ui.ViewModelActivity;
import org.cerion.stocklist.charts.IndicatorChart;
import org.cerion.stocklist.charts.PriceChart;
import org.cerion.stocklist.charts.StockChart;
import org.cerion.stocklist.charts.VolumeChart;
import org.cerion.stocklist.indicators.MACD;

public class ChartsActivity extends ViewModelActivity<ChartsViewModel> {
    private static final String TAG = ChartsActivity.class.getSimpleName();
    private static final String EXTRA_SYMBOL = "symbol";

    private LinearLayout mCharts;
    private RangeBar rangeBar;

    public static Intent newIntent(Context context, String symbol) {
        Intent intent = new Intent(context,ChartsActivity.class);
        intent.putExtra(ChartsActivity.EXTRA_SYMBOL, symbol);
        return intent;
    }

    @Override
    protected ChartsViewModel newViewModel() {
        return new ChartsViewModel(getIntent().getStringExtra(EXTRA_SYMBOL), Injection.getAPI(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityChartsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_charts);
        binding.setViewmodel(getViewModel());

        rangeBar = binding.rangeBar;
        mCharts = binding.charts;

        rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int i, int i1) {
                Log.d(TAG, i + " " + i1);
                for(int n = 0; n < mCharts.getChildCount(); n++) {
                    ChartView cv = (ChartView)mCharts.getChildAt(n);
                    cv.setRange(i, i1);
                }
            }
        });

        getViewModel().priceList.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                int size = getViewModel().priceList.get().size();
                rangeBar.setTickCount(size);
                rangeBar.setThumbIndices(0, size-1);
            }
        });

        // Restore previous charts
        if (IsRetained()) {
            for(ChartViewModel vm : getViewModel().charts) {
                ChartView view = new ChartView(this, vm);
                mCharts.addView(view);
            }
        } else {
            addPriceChart();
        }

        binding.fabGroup.setListener(getViewModel());

        binding.fabGroup.add("Price", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPriceChart();
            }
        });

        binding.fabGroup.add("Volume", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddChart(new VolumeChart());
            }
        });

        binding.fabGroup.add("Indicator", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddChart(new IndicatorChart(new MACD()));
            }
        });

        if (getViewModel().fabOpen.get())
            binding.fabGroup.open();
    }

    public void addPriceChart() {
        PriceChart chart = new PriceChart();
        chart.candleData = false;
        onAddChart(chart);
    }

    private void onAddChart(StockChart chart) {
        final ChartViewModel vm = new ChartViewModel(getViewModel(), chart);
        final ChartView view = new ChartView(this, vm);

        mCharts.addView(view);
        getViewModel().charts.add(vm);

        vm.setOnRemoveListener(new ChartViewModel.OnRemoveListener() {
            @Override
            public void onRemove() {
                getViewModel().charts.remove(vm);
                mCharts.removeView(view);
            }
        });

        if (chart instanceof IndicatorChart)
            view.edit();
    }
}
