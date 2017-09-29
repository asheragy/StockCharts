package org.cerion.stockcharts.charts.views;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.charts.ChartViewModel;
import org.cerion.stockcharts.charts.EditChartDialog;
import org.cerion.stockcharts.charts.IChartView;
import org.cerion.stockcharts.databinding.ViewChartBinding;

public class ChartView extends LinearLayout implements IChartView {

    protected ChartViewFactory chartFactory;
    protected ChartViewModel viewModel;
    protected ViewChartBinding binding;
    protected Chart chart;

    public ChartView(Context context, ChartViewModel viewModel) {
        super(context);

        this.viewModel = viewModel;
        viewModel.setView(this);
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_chart, this, true);
        binding.setViewmodel(this.viewModel);

        chartFactory = new ChartViewFactory(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        reload();
    }

    public void edit() {
        FragmentManager fm = ((Activity)getContext()).getFragmentManager();
        EditChartDialog dialog = EditChartDialog.newInstance(viewModel.getChart(), viewModel);
        dialog.show(fm, "editDialog");
    }

    public void setRange(int start, int end) {
        if (chart != null) {
            BarLineChartBase c = (BarLineChartBase) chart;

            c.setVisibleXRangeMinimum(end - start);
            c.setVisibleXRangeMaximum(end - start);
            c.moveViewToX(start);
        }
    }

    protected void setChart(Chart chart) {
        this.chart = chart;

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

    public void reload() {
        Chart chart;

        if (viewModel.getList() != null)
            chart = chartFactory.getChart(viewModel.getChart(), viewModel.getList());
        else
            chart = chartFactory.getEmptyChart();

        setChart(chart);
    }
}
