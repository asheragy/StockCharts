package org.cerion.stockcharts.charts;

import android.content.Context;
import android.view.View;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.Chart;

import org.cerion.stockcharts.R;
import org.cerion.stocklist.charts.VolumeChart;
import org.cerion.stocklist.functions.IOverlay;
import org.cerion.stocklist.functions.IPriceOverlay;
import org.cerion.stocklist.functions.ISimpleOverlay;
import org.cerion.stocklist.model.Interval;

class ChartHolderVolume extends ChartHolderBase {

    private static final String TAG = ChartHolderIndicator.class.getSimpleName();

    public ChartHolderVolume(Context context, String symbol, VolumeChart chart) {
        super(context, symbol, chart);
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
