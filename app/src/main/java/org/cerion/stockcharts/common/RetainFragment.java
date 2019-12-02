package org.cerion.stockcharts.common;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

public class RetainFragment<T> extends Fragment {
    public T data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /*
    public static <T> RetainFragment<T> findOrCreate(FragmentManager fm, String tag) {
        RetainFragment<T> retainFragment = (RetainFragment<T>) fm.findFragmentByTag(tag);

        if(retainFragment == null){
            retainFragment = new RetainFragment<>();
            fm.beginTransaction()
                    .add(retainFragment, tag)
                    .commitAllowingStateLoss();
        }

        return retainFragment;
    }

    // Remove from FragmentManager
    public void remove(FragmentManager fm) {
        if(!fm.isDestroyed()){
            fm.beginTransaction()
                    .remove(this)
                    .commitAllowingStateLoss();
            data = null;
        }
    }
    */
}
