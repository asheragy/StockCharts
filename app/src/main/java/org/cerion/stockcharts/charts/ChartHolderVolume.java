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

    public ChartHolderVolume(Context context, String symbol, Interval interval) {
        super(context, symbol, interval);

        mStockChart = new VolumeChart();
        mStockChart.interval = interval;
        // Fill spinner
        Spinner sp = (Spinner)findViewById(R.id.function);
        sp.setVisibility(View.GONE);

        init();
        reload();
    }

    private VolumeChart volumeChart() {
        return (VolumeChart)mStockChart;
    }

    private void init() {

        findViewById(R.id.save_edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View controls = findViewById(R.id.edit_layout);

                if(controls.getVisibility() == View.VISIBLE) { // SAVE

                    mStockChart.clearOverlays();

                    // Get overlay parameters
                    for(int i = 0; i < mOverlays.getChildCount(); i++) {
                        OverlayEditControl editControl = (OverlayEditControl)mOverlays.getChildAt(i);

                        // TODO see if there is a way to avoid this cast
                        volumeChart().addOverlay((ISimpleOverlay)editControl.getOverlayFunction());
                    }

                    volumeChart().logScale = mCheckLogScale.isChecked();
                    reload();
                    setInEditMode(false);
                } else {
                    setInEditMode(true);
                }
            }
        });
    }

}
