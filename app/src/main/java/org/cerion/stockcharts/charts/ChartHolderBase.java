package org.cerion.stockcharts.charts;

import android.content.Context;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.Chart;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stocklist.model.Interval;

public abstract class ChartHolderBase extends ParametersEditControl {

    protected LinearLayout mOverlays;
    protected ChartFactory mChartFactory;
    protected ChartParams mChartParams;

    public ChartHolderBase(Context context, String symbol, Interval interval) {
        super(context, R.layout.view_chart_holder);

        mOverlays = (LinearLayout)findViewById(R.id.overlays);
        mOverlays.removeAllViews(); // remove placeholder used in design viewer

        mChartFactory = new ChartFactory(context);
        mChartParams = new ChartParams(symbol, interval);

        setInEditMode(false);
    }

    public abstract Chart getChart();

    public void setOnRemoveClickListener(OnClickListener listener) {
        findViewById(R.id.remove).setOnClickListener(listener);
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

    protected void onAddOverlay() {
        final OverlayEditControl control = new OverlayEditControl(getContext());
        control.setOnDelete(new OverlayEditControl.OnDeleteListener() {
            @Override
            public void delete() {
                mOverlays.removeView(control);
            }
        });

        mOverlays.addView(control);
    }

    public void reload(Interval interval) {
        mChartParams.interval = interval;
        reload();
    }

    protected void reload() {
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