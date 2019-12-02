package org.cerion.stockcharts.common;

import androidx.databinding.ObservableList;

public abstract class OnListAnyChangeCallback<T extends ObservableList> extends ObservableList.OnListChangedCallback<T> {

    public abstract void onAnyChange(ObservableList sender);

    @Override
    public void onChanged(ObservableList sender) {
        onAnyChange(sender);
    }

    @Override
    public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
        onAnyChange(sender);
    }

    @Override
    public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
        onAnyChange(sender);
    }

    @Override
    public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {
        onAnyChange(sender);
    }

    @Override
    public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
        onAnyChange(sender);
    }
}
