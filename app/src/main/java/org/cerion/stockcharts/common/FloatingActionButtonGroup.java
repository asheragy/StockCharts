package org.cerion.stockcharts.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.cerion.stockcharts.R;

public class FloatingActionButtonGroup extends RelativeLayout {

    private RelativeLayout buttons;
    private boolean isOpen;
    private TextView master;
    private View overlay;
    private FabStateListener listener;

    public interface FabStateListener {
        void onStateChange(boolean open);
    }

    public FloatingActionButtonGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_fab_group, this, true);

        buttons = (RelativeLayout) view.findViewById(R.id.buttons);
        overlay = view.findViewById(R.id.fab_overlay);
        master = (TextView)view.findViewById(R.id.text);
    }

    public void setListener(FabStateListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close(true);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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
    }

    public void add(String text, final OnClickListener listener) {
        ChildFloatingActionButton button = new ChildFloatingActionButton(getContext(), text);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
                close(false);
            }
        });

        button.setVisibility(View.GONE);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.ALIGN_PARENT_END);

        buttons.addView(button, lp);
    }


    public void open() {
        isOpen=true;

        float shift = getResources().getDimension(R.dimen.fab_small) + (getResources().getDimension(R.dimen.fab_small_margin) / 2);
        float diff = getResources().getDimension(R.dimen.fab_margin);

        for(int i = buttons.getChildCount() - 1; i >= 0; i--) {
            buttons.getChildAt(i).setVisibility(View.VISIBLE);
            buttons.getChildAt(i).animate().translationY(-shift * (i + 1) -diff);
        }

        overlay.setVisibility(View.VISIBLE);
        master.setText("X");

        if (listener != null)
            listener.onStateChange(true);
    }

    private void close(boolean animate) {
        isOpen=false;

        if (!animate) {
            for(int i = 0; i < buttons.getChildCount(); i++)
                buttons.getChildAt(i).setVisibility(View.GONE);
        }

        for(int i = 0; i < buttons.getChildCount(); i++) {
            final View v = buttons.getChildAt(i);
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
        master.setText("+");

        if (listener != null)
            listener.onStateChange(false);
    }
}
