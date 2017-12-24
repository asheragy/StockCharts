package org.cerion.stockcharts.charts;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cerion.stockcharts.Injection;
import org.cerion.stockcharts.common.ViewModelFragment;
import org.cerion.stockcharts.databinding.FragmentSummaryPanelBinding;

public class InfoPanelFragment extends ViewModelFragment<InfoPanelViewModel> {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        FragmentSummaryPanelBinding binding = FragmentSummaryPanelBinding.inflate(inflater, container, false);
        binding.setViewModel(getViewModel());
        return binding.getRoot();
    }

    @Override
    protected InfoPanelViewModel newViewModel() {
        return new InfoPanelViewModel(Injection.getAPI(getContext()));
    }

    public void load(String symbol) {
        getViewModel().load(symbol);
    }
}
