package org.cerion.stockcharts.charts.views;

import android.content.Context;

import org.cerion.stockcharts.charts.ChartViewModel;
import org.cerion.stocklist.charts.PriceChart;

class PriceChartView extends ChartView {

    private static final String TAG = IndicatorChartView.class.getSimpleName();

    PriceChartView(Context context, ChartViewModel viewModel) {
        super(context, viewModel);
    }

    @Override
    protected void onSave() {
        PriceChart chart = (PriceChart)mStockChart;
        mStockChart.clearOverlays();

        // Get overlay parameters
        for(int i = 0; i < mOverlays.getChildCount(); i++) {
            OverlayEditControl editControl = (OverlayEditControl)mOverlays.getChildAt(i);
            chart.addOverlay(editControl.getOverlayFunction());
        }

        chart.logScale = mViewModel.logScale.get();
        chart.candleData = !mViewModel.lineChart.get();
    }
}
