package org.cerion.stockcharts.charts.views;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.mikephil.charting.charts.Chart;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.charts.ChartViewModel;
import org.cerion.stockcharts.charts.EditChartDialog;
import org.cerion.stockcharts.charts.IChartView;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.databinding.ViewChartBinding;
import org.cerion.stocklist.charts.StockChart;

public class ChartView extends LinearLayout implements IChartView, EditChartDialog.ChartChangeListener {

    protected ChartViewFactory chartFactory;
    protected ChartViewModel viewModel;
    protected ViewChartBinding binding;

    public ChartView(Context context, ChartViewModel viewModel) {
        super(context);

        this.viewModel = viewModel;
        viewModel.setView(this);
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_chart, this, true);
        binding.setViewmodel(this.viewModel);

        chartFactory = new ChartViewFactory(context);
    }

    @Override
    public void chartChanged(StockChart chart) {
        // TODO see if all this can be moved from viewmodel -> viewmodel calls
        viewModel.setChart(chart);
    }

    @Override
    public void chartRemoved() {
        ViewGroup viewGroup = (ViewGroup)getParent();
        viewGroup.removeView(ChartView.this);

        // Remove viewmodel from parent list
        viewModel.getParent().charts.remove(viewModel);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        reload();
    }

    public void edit() {
        FragmentManager fm = ((Activity)getContext()).getFragmentManager();
        EditChartDialog dialog = EditChartDialog.newInstance(viewModel.getChart(), ChartView.this);
        dialog.show(fm, "editDialog");
    }

    protected void setChart(Chart chart) {
        final FrameLayout frame = (FrameLayout) findViewById(R.id.chart_frame);
        frame.removeAllViews();
        frame.addView(chart);

        chart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                edit();
            }
        });
    }

    private Chart getChart() {
        try {
            return chartFactory.getChart(this.viewModel);
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return chartFactory.getEmptyChart();
        }
    }

    public void reload() {
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
