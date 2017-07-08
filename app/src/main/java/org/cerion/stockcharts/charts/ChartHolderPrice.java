package org.cerion.stockcharts.charts;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;

import com.github.mikephil.charting.charts.Chart;

import org.cerion.stockcharts.R;
import org.cerion.stocklist.charts.PriceChart;
import org.cerion.stocklist.functions.IPriceOverlay;
import org.cerion.stocklist.model.Interval;

class ChartHolderPrice extends ChartHolderBase {

    private static final String TAG = ChartHolderIndicator.class.getSimpleName();

    public ChartHolderPrice(Context context, String symbol, PriceChart chart) {
        super(context, symbol, chart);
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
