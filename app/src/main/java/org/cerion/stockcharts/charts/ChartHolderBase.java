package org.cerion.stockcharts.charts;

import android.content.Context;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.Chart;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stocklist.charts.StockChart;
import org.cerion.stocklist.model.Interval;

public abstract class ChartHolderBase extends ParametersEditControl {

    protected LinearLayout mOverlays;
    protected ChartFactory mChartFactory;
    protected StockChart mStockChart;
    protected String mSymbol;
    protected CheckBox mCheckLogScale;

    public ChartHolderBase(Context context, String symbol, StockChart chart) {
        super(context, R.layout.view_chart_holder);

        mSymbol = symbol;
        mStockChart = chart;

        mCheckLogScale = (CheckBox)findViewById(R.id.check_logscale);
        mOverlays = (LinearLayout)findViewById(R.id.overlays);
        mOverlays.removeAllViews(); // remove placeholder used in design viewer

        mChartFactory = new ChartFactory(context);

        findViewById(R.id.add_overlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddOverlay();
            }
        });

        setInEditMode(false);
    }

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
        return mChartFactory.getChart(mStockChart, mSymbol);
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
