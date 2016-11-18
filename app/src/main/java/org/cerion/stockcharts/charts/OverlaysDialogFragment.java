package org.cerion.stockcharts.charts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import org.cerion.stocklist.model.Overlay;

public class OverlaysDialogFragment extends DialogFragment {

    public interface OnSelectListener {
        void select(OverlayDataSet overlay);
    }

    public static OverlaysDialogFragment newInstance(int title) {
        OverlaysDialogFragment frag = new OverlaysDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");

        final OverlaysDialogFragment.OnSelectListener listener = (OverlaysDialogFragment.OnSelectListener)getActivity();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.select_dialog_item);

        for(Overlay o : Overlay.values()) {
            arrayAdapter.add(o.toString());
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        OverlayDataSet result = null;
                        Overlay o = Overlay.values()[which];
                        switch(o)
                        {
                            case EMA:
                                result = OverlayDataSet.getEMA(20);
                                break;
                            case SMA:
                                result = OverlayDataSet.getSMA(20);
                                break;
                            case BB:
                                result = OverlayDataSet.getBB(20, 2.0f);
                                break;
                            case KAMA:
                                result = OverlayDataSet.getKAMA(10, 2, 30);
                        }

                        listener.select(result);
                        dialog.dismiss();
                    }
                })
                .create();
    }
}
