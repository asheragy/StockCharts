package org.cerion.stockcharts.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import org.cerion.stockcharts.common.RetainFragment;

public abstract class ViewModelActivity<T> extends AppCompatActivity {

    private T mViewModel;
    private RetainFragment<T> mRetainFragment;
    private static final String RETAINED_FRAGMENT = "RetainedFragment";
    private boolean mIsRetained = false;

    protected T getViewModel() {
        return mViewModel;
    }

    protected abstract T newViewModel();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();
        mRetainFragment = (RetainFragment<T>) fm.findFragmentByTag(RETAINED_FRAGMENT);

        if (mRetainFragment == null) {
            mRetainFragment = new RetainFragment<>();
            mRetainFragment.data = newViewModel();
            mViewModel = mRetainFragment.data;
            fm.beginTransaction().add(mRetainFragment, RETAINED_FRAGMENT).commit();
        } else {
            mViewModel = mRetainFragment.data;
            mIsRetained = true;
        }
    }

    protected boolean IsRetained() {
        return mIsRetained;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(mRetainFragment)
                    .commit();
        }
    }
}
