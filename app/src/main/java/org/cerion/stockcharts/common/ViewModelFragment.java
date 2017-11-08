package org.cerion.stockcharts.common;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

public abstract class ViewModelFragment<T> extends Fragment {

    private T viewModel;
    private boolean retained = false;

    protected T getViewModel() {
        return viewModel;
    }

    protected abstract T newViewModel();

    protected boolean isRetained() {
        return retained;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        viewModel = newViewModel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // When fragment survives after this it means it was retained
        retained = true;
    }
}
