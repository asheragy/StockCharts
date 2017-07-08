package org.cerion.stockcharts.charts;

import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.cerion.stockcharts.R;
import org.cerion.stocklist.charts.IndicatorChart;
import org.cerion.stocklist.functions.IIndicator;
import org.cerion.stocklist.functions.ISimpleOverlay;

class ChartHolderIndicator extends ChartHolderBase implements ChartViewModel.OnFunctionChangeListener {

    private static final String TAG = ChartHolderIndicator.class.getSimpleName();

    public ChartHolderIndicator(Context context, String symbol, IndicatorChart chart) {
        super(context, symbol, chart);

        mViewModel.setFunctionListener(this);

        onFunctionChanged(); // Set to default
        //reload();
    }

    private IndicatorChart indicatorChart() {
        return (IndicatorChart)mStockChart;
    }

    @Override
    public void onFunctionChanged() {
        setIndicator(mViewModel.getFunction());
    }

    private void setIndicator(final IIndicator instance) {
        //Indicator indicator = .getId();

        // Reset selection
        //final IIndicator instance = indicator.getInstance();
        final Number[] params = instance.params();
        final EditText[] fields = new EditText[params.length];

        indicatorChart().setIndicator(instance);

        // If overlay is not allowed then hide it
        if(!mViewModel.showAddOverlay.get()) {
            mOverlays.removeAllViews();
        }

        // Add parameters
        LinearLayout layout = (LinearLayout)findViewById(R.id.parameters);
        layout.removeAllViews();
        for(int i = 0; i < params.length; i++) {
            Number n = params[i];
            fields[i] = getInputField(n);
            layout.addView(fields[i]);
        }
    }

    @Override
    protected void onSave() {
        final IIndicator instance = mViewModel.getFunction();
        final Number[] params = instance.params();
        //final EditText[] fields = new EditText[params.length];

        indicatorChart().setIndicator(instance);

        //Get parameters and redraw chart
        if (params.length > 0) {
            Number p[] = getParameters(params);
            instance.setParams(p);
            indicatorChart().setIndicator( instance );
        }

        mStockChart.clearOverlays();

        // Get overlay parameters
        for(int i = 0; i < mOverlays.getChildCount(); i++) {
            OverlayEditControl editControl = (OverlayEditControl)mOverlays.getChildAt(i);
            indicatorChart().addOverlay((ISimpleOverlay)editControl.getOverlayFunction());
        }
    }
}
