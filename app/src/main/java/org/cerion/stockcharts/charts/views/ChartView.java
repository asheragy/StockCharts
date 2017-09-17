package org.cerion.stockcharts.charts.views;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import org.cerion.stockcharts.databinding.ViewChartHolderBinding;
import org.cerion.stocklist.charts.IndicatorChart;
import org.cerion.stocklist.charts.PriceChart;
import org.cerion.stocklist.charts.StockChart;

public abstract class ChartView extends LinearLayout implements IChartView, EditChartDialog.ChartChangeListener {

    protected LinearLayout mOverlays;
    protected ChartViewFactory mChartFactory;
    protected StockChart mStockChart; // TODO redundant, same as view model
    protected ChartViewModel mViewModel;
    protected String mSymbol;
    protected ViewChartHolderBinding binding;

    public static ChartView getInstance(Context context, ChartViewModel viewModel) {
        StockChart chart = viewModel.getChart();

        if (chart instanceof  PriceChart)
            return new PriceChartView(context, viewModel);
        else if (chart instanceof IndicatorChart)
            return new IndicatorChartView(context, viewModel);
        else
            return new VolumeChartView(context, viewModel);
    }

    @Override
    public void chartChanged(StockChart chart) {
        mStockChart = chart;
        reload();
    }

    @Override
    public void chartRemoved() {
        ViewGroup viewGroup = (ViewGroup)getParent();
        viewGroup.removeView(ChartView.this);

        // Remove viewmodel from parent list
        mViewModel.getParent().charts.remove(mViewModel);
    }

    protected ChartView(Context context, final ChartViewModel viewModel) {
        //super(context, R.layout.view_chart_holder);
        super(context);

        viewModel.setView(this);

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_chart_holder, this, true);
        //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //binding = ViewChartHolderBinding.inflate(inflater);

        mViewModel = viewModel;
        binding.setViewmodel(mViewModel);

        mSymbol = viewModel.getParent().getSymbol();
        mStockChart = viewModel.getChart();

        mOverlays = (LinearLayout)findViewById(R.id.overlays);
        mOverlays.removeAllViews(); // remove placeholder used in design viewer

        mChartFactory = new ChartViewFactory(context);

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

        binding.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewGroup viewGroup = (ViewGroup)getParent();
                viewGroup.removeView(ChartView.this);

                // Remove viewmodel from parent list
                viewModel.getParent().charts.remove(viewModel);
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

    public StockChart getStockChart() {
        return mStockChart;
    }

    public void edit() {
        FragmentManager fm = ((Activity)getContext()).getFragmentManager();
        EditChartDialog dialog = EditChartDialog.newInstance(mStockChart, ChartView.this);
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
        if(getClass() == PriceChartView.class)
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
            return mChartFactory.getChart(this.mViewModel);
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT);
            return mChartFactory.getEmptyChart();
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
