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
    protected LinearLayout mOverlays; // TODO use binding
    private View view;
    //private StockChart chart;
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

        DialogChartEditBinding binding = DialogChartEditBinding.bind(view);
        binding.setViewmodel(viewModel);

        //binding.addOverlay.setVisibility(View.VISIBLE);

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
                    //IndicatorChart ichart = (IndicatorChart)chart;

                    final IIndicator instance = viewModel.getFunction();
                    //final Number[] params = instance.params();
                    instance.setParams( getParametersControl().getParameters() );

                    //ichart.setIndicator(instance);
                    //final EditText[] fields = new EditText[params.length];

                    indicatorChart().setIndicator(instance);

                    //Get parameters and redraw chart
                    /*
                    if (params.length > 0) {
                        Number p[] = getParameters(params);
                        instance.setParams(p);
                        indicatorChart().setIndicator( instance );
                    }
                    */
                }

                // Get overlay parameters
                for(int i = 0; i < mOverlays.getChildCount(); i++) {
                    OverlayEditControl editControl = (OverlayEditControl)mOverlays.getChildAt(i);

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

        mOverlays = (LinearLayout)view.findViewById(R.id.overlays);
        mOverlays.removeAllViews(); // remove placeholder used in design viewer

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
        indicatorChart().setIndicator(instance);

        // If overlay is not allowed then hide it
        if(!viewModel.showAddOverlay.get()) {
            mOverlays.removeAllViews();
        }

        // Add parameters
        final Number[] params = instance.params();
        getParametersControl().setParameters(params);
    }

    private ParametersEditControl getParametersControl() {
        if (getChart() instanceof IndicatorChart)
            return (ParametersEditControl) view.findViewById(R.id.parameters);

        throw new RuntimeException();
    }

    private StockChart getChart() {
        return viewModel.getChart();
    }

    private OverlayEditControl onAddOverlay() {

        final OverlayEditControl control;
        //if(viewModel.getChart() instanceof PriceChart)
            control = new OverlayEditControl(getContext(), viewModel.getChart().getOverlays());
        //else
        //    control = new OverlayEditControl(getContext(), mStockChart.getOverlays());


        control.setOnDelete(new OverlayEditControl.OnDeleteListener() {
            @Override
            public void delete() {
                mOverlays.removeView(control);
            }
        });

        mOverlays.addView(control);

        return control;
    }
}
