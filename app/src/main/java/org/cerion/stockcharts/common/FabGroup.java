package org.cerion.stockcharts.common;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.View;

import org.cerion.stockcharts.R;

import java.util.ArrayList;
import java.util.List;

public class FabGroup {

    private List<View> children = new ArrayList<>();
    private boolean isOpen;
    private Context context;
    private View overlay;
    private MasterFloatingActionButton master;
    private FabViewStateListener listener;

    public interface FabViewStateListener {
        void setOpen(boolean open);
    }

    public FabGroup(MasterFloatingActionButton fab, View overlay, FabViewStateListener listener) {
        this.context = fab.getContext();
        this.master = fab;
        this.overlay = overlay;
        this.listener = listener;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isOpen){
                    open();
                }else{
                    close(true);
                }
            }
        });

        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close(true);
            }
        });
    }

    public void addFab(final View view, final View.OnClickListener listener) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close(false);
                listener.onClick(v);
            }
        });

        children.add(view);
    }

    public void open() {
        isOpen=true;

        float shift = context.getResources().getDimension(R.dimen.fab_small) + (context.getResources().getDimension(R.dimen.fab_small_margin) / 2);

        for(View v : children)
            v.setVisibility(View.VISIBLE);

        for(int i = 1; i <= children.size(); i++) {
            children.get(i-1).animate().translationY(-shift * i);
        }

        overlay.setVisibility(View.VISIBLE);
        master.open();

        if (listener != null)
            listener.setOpen(true);
    }

    private void close(boolean animate) {
        isOpen=false;

        if (!animate) {
            for(View v : children)
                v.setVisibility(View.GONE);
        }

        for(final View v : children) {
            v.animate().translationY(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    v.setVisibility(View.GONE);
                    v.animate().setListener(null);
                }
            });
        }

        overlay.setVisibility(View.GONE);
        master.close();

        if (listener != null)
            listener.setOpen(false);
    }
}
