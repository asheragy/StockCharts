package org.cerion.stockcharts.charts.views;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.databinding.ViewSummaryPanelBinding;

public class SummaryPanelView extends LinearLayout {

    ViewSummaryPanelBinding binding;

    public SummaryPanelView(Context context) {
        super(context);

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_summary_panel, this, true);
    }
}
