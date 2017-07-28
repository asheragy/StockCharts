package org.cerion.stockcharts.charts.views;

import android.content.Context;

import org.cerion.stockcharts.charts.ChartViewModel;
import org.cerion.stocklist.charts.VolumeChart;
import org.cerion.stocklist.functions.ISimpleOverlay;

class VolumeChartView extends ChartView {

    private static final String TAG = IndicatorChartView.class.getSimpleName();

    VolumeChartView(Context context, ChartViewModel viewModel) {
        super(context, viewModel);
    }

    @Override
    protected void onSave() {
        VolumeChart chart = (VolumeChart)mStockChart;
        mStockChart.clearOverlays();

        // Get overlay parameters
        for(int i = 0; i < mOverlays.getChildCount(); i++) {
            OverlayEditControl editControl = (OverlayEditControl)mOverlays.getChildAt(i);

            // TODO see if there is a way to avoid this cast
            chart.addOverlay((ISimpleOverlay)editControl.getOverlayFunction());
        }

        chart.logScale = mViewModel.logScale.get();
    }
}
