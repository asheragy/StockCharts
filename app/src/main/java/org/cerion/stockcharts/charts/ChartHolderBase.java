package org.cerion.stockcharts.charts;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.mikephil.charting.charts.Chart;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.databinding.ViewChartHolderBinding;
import org.cerion.stocklist.charts.StockChart;
import org.cerion.stocklist.model.Interval;

public abstract class ChartHolderBase extends ParametersEditControl {

    protected LinearLayout mOverlays;
    protected ChartFactory mChartFactory;
    protected StockChart mStockChart;
    protected ChartViewModel mViewModel;
    protected String mSymbol;
    protected ViewChartHolderBinding mBinding;

    public ChartHolderBase(Context context, String symbol, StockChart chart) {
        //super(context, R.layout.view_chart_holder);
        super(context);

        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_chart_holder, this, true);
        //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //mBinding = ViewChartHolderBinding.inflate(inflater);

        mViewModel = new ChartViewModel(chart);
        mBinding.setViewmodel(mViewModel);

        mSymbol = symbol;
        mStockChart = chart;

        mOverlays = (LinearLayout)findViewById(R.id.overlays);
        mOverlays.removeAllViews(); // remove placeholder used in design viewer

        mChartFactory = new ChartFactory(context);

        findViewById(R.id.add_overlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddOverlay();
            }
        });

        findViewById(R.id.save_edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View controls = findViewById(R.id.edit_layout);
                if(controls.getVisibility() == View.VISIBLE) { // SAVE
                    onSave();
                    reload();
                    setInEditMode(false);
                } else {
                    setInEditMode(true);
                }
            }
        });

        setInEditMode(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        reload();
    }

    protected abstract void onSave();

    public void setOnRemoveClickListener(OnClickListener listener) {
        findViewById(R.id.remove).setOnClickListener(listener);
    }

    public StockChart getStockChart() {
        return mStockChart;
    }

    protected void setChart(Chart chart) {
        final FrameLayout frame = (FrameLayout) findViewById(R.id.chart_frame);
        frame.removeAllViews();
        frame.addView(chart);
    }

    protected void setInEditMode(boolean bEdit) {
        Button button = (Button)findViewById(R.id.save_edit_button);
        View controls = findViewById(R.id.edit_layout);

        if(bEdit) {
            button.setText("Save");
            controls.setVisibility(View.VISIBLE);
        } else {
            button.setText("Edit");
            controls.setVisibility(View.GONE);
        }

    }

    private void onAddOverlay() {

        final OverlayEditControl control;
        if(getClass() == ChartHolderPrice.class)
            control = new OverlayEditControl(getContext(), mStockChart.getOverlays());
        else
            control = new OverlayEditControl(getContext(), mStockChart.getOverlays());

        control.setOnDelete(new OverlayEditControl.OnDeleteListener() {
            @Override
            public void delete() {
                mOverlays.removeView(control);
            }
        });

        mOverlays.addView(control);

    }

    private Chart getChart() {
        try {
            return mChartFactory.getChart(mStockChart, mSymbol);
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT);
            return mChartFactory.getEmptyChart();
        }
    }

    public void reload(Interval interval) {
        mStockChart.interval = interval;
        reload();
    }

    protected void reload() {
        // TODO see if interval can be set ONLY when this function is called

        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.loading_progressBar);
        progressBar.setVisibility(View.VISIBLE);

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {

            Chart chart;
            @Override
            public void run() {
                if (Looper.myLooper() == null)
                    Looper.prepare(); // This is needed to create a new chart instance inside a different thread

                chart = getChart();
                if( Looper.myLooper() != null)
                    Looper.myLooper().quit();
            }

            @Override
            public void onFinish() {
                setChart(chart);
                progressBar.setVisibility(View.GONE);
            }
        });

        task.execute();
    }
}
