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

        if (viewModel != null)
            retained = true;
        else
            viewModel = newViewModel();
    }
}
