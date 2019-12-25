package org.cerion.stockcharts.charts;


import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.charts.views.OverlayEditControl;
import org.cerion.stockcharts.charts.views.ParametersEditControl;
import org.cerion.stockcharts.databinding.DialogChartEditBinding;
import org.cerion.stocks.core.charts.IndicatorChart;
import org.cerion.stocks.core.charts.PriceChart;
import org.cerion.stocks.core.charts.StockChart;
import org.cerion.stocks.core.functions.IIndicator;
import org.cerion.stocks.core.functions.IOverlay;
import org.cerion.stocks.core.functions.ISimpleOverlay;

import java.util.List;

public class EditChartDialog extends DialogFragment implements EditChartViewModel.OnFunctionChangeListener {

    private EditChartViewModel viewModel;
    private DialogChartEditBinding binding;
    private LinearLayout overlays;
    private ChartViewModel chartViewModel;

    public static EditChartDialog newInstance(StockChart chart, ChartViewModel chartViewModel) {
        EditChartDialog dialog = new EditChartDialog();
        dialog.viewModel = new EditChartViewModel(chart);
        dialog.chartViewModel = chartViewModel;
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_chart_edit, container);

        binding = DialogChartEditBinding.bind(view);
        //binding.setViewmodel(viewModel);

        // TODO add as binding
        binding.addOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddOverlay();
            }
        });

        binding.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chartViewModel.remove();
                dismiss();
            }
        });

        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateChart();
                chartViewModel.setChart(viewModel.getChart());
                dismiss();
            }
        });

        overlays = binding.overlays;
        overlays.removeAllViews(); // remove placeholder used in design viewer

        viewModel.setFunctionListener(EditChartDialog.this);
        if (getChart() instanceof IndicatorChart) {
            setIndicator( ((IndicatorChart)getChart()).getIndicator());
        }

        for(int i = 0; i < viewModel.getChart().getOverlayCount(); i++) {
            IOverlay overlay = viewModel.getChart().getOverlay(i);
            onAddOverlay().setOverlayFunction(overlay);
        }

        return view;
    }

    /*
    @Override
    public void onPause() {
        super.onPause();

        // Overlays are not working with binding yet so manually update them before rotation
        updateChart();
    }
    */

    private void updateChart() {
        StockChart chart = viewModel.getChart();
        chart.clearOverlays();

        // TODO overlays and parameters all need to be in viewmodel
        /*
        if (chart instanceof IndicatorChart) {
            final IIndicator instance = viewModel.getFunction();
            instance.setParams( getParametersControl().getParameters() );
            indicatorChart().setIndicator(instance);
        }
        */

        // Get overlay parameters
        for(int i = 0; i < overlays.getChildCount(); i++) {
            OverlayEditControl editControl = (OverlayEditControl) overlays.getChildAt(i);

            if (chart instanceof PriceChart) {
                ((PriceChart) chart).addOverlay(editControl.getOverlayFunction());

            }
            else
                chart.addOverlay((ISimpleOverlay)editControl.getOverlayFunction());
        }
    }

    @Override
    public void onDestroyView() {
        // Don't dismiss on rotate
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    private IndicatorChart indicatorChart() {
        return (IndicatorChart)viewModel.getChart();
    }

    @Override
    public void onFunctionChanged() {
        setIndicator(viewModel.getFunction());
    }

    private void setIndicator(final IIndicator instance) {
        if (indicatorChart().getIndicator() != instance)
            indicatorChart().setIndicator(instance);

        // If overlay is not allowed then hide it
        if(!viewModel.showAddOverlay.get()) {
            overlays.removeAllViews();
        }

        // Add parameters
        final List<Number> params = instance.getParams();
        getParametersControl().setParameters(params);
    }

    private ParametersEditControl getParametersControl() {
        if (getChart() instanceof IndicatorChart)
            return binding.parameters;

        throw new RuntimeException();
    }

    private StockChart getChart() {
        return viewModel.getChart();
    }

    private OverlayEditControl onAddOverlay() {
        final OverlayEditControl control = new OverlayEditControl(getContext(), viewModel.getChart().getOverlays());
        control.setOnDelete(new OverlayEditControl.OnDeleteListener() {
            @Override
            public void delete() {
                overlays.removeView(control);
            }
        });

        overlays.addView(control);
        return control;
    }
}
