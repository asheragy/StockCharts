package org.cerion.stockcharts.ui.charts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.cerion.stockcharts.databinding.FragmentSummaryPanelBinding;

public class InfoPanelFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        FragmentSummaryPanelBinding binding = FragmentSummaryPanelBinding.inflate(inflater, container, false);
        //binding.setViewModel(getViewModel());
        return binding.getRoot();
    }
}
