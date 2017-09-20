package org.cerion.stockcharts.charts;


import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.charts.views.OverlayEditControl;
import org.cerion.stockcharts.charts.views.ParametersEditControl;
import org.cerion.stockcharts.databinding.DialogChartEditBinding;
import org.cerion.stocklist.charts.IndicatorChart;
import org.cerion.stocklist.charts.PriceChart;
import org.cerion.stocklist.charts.StockChart;
import org.cerion.stocklist.functions.IIndicator;
import org.cerion.stocklist.functions.IOverlay;
import org.cerion.stocklist.functions.ISimpleOverlay;

public class EditChartDialog extends DialogFragment implements EditChartViewModel.OnFunctionChangeListener {

    private EditChartViewModel viewModel;
    private DialogChartEditBinding binding;
    private LinearLayout overlays;
    private View view;
    private ChartChangeListener listener;

    public interface ChartChangeListener {
        void chartChanged(StockChart chart);
        void chartRemoved();
    }

    public static EditChartDialog newInstance(StockChart chart, ChartChangeListener listener) {
        EditChartDialog dialog = new EditChartDialog();
        dialog.listener = listener;
        dialog.viewModel = new EditChartViewModel(chart);
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
        this.view = inflater.inflate(R.layout.dialog_chart_edit, container);

        binding = DialogChartEditBinding.bind(view);
        binding.setViewmodel(viewModel);

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
                listener.chartRemoved();
                dismiss();
            }
        });

        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StockChart chart = viewModel.getChart();
                chart.clearOverlays();

                if (chart instanceof PriceChart) {
                    ((PriceChart) chart).logScale = viewModel.logScale.get();
                    ((PriceChart) chart).candleData = !viewModel.lineChart.get();
                } else if (chart instanceof IndicatorChart) {
                    final IIndicator instance = viewModel.getFunction();
                    instance.setParams( getParametersControl().getParameters() );
                    indicatorChart().setIndicator(instance);
                }

                // Get overlay parameters
                for(int i = 0; i < overlays.getChildCount(); i++) {
                    OverlayEditControl editControl = (OverlayEditControl) overlays.getChildAt(i);

                    if (chart instanceof PriceChart) {
                        ((PriceChart) chart).addOverlay(editControl.getOverlayFunction());

                    }
                    else
                        chart.addOverlay((ISimpleOverlay)editControl.getOverlayFunction());
                }

                listener.chartChanged(chart);
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
        final Number[] params = instance.params();
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
