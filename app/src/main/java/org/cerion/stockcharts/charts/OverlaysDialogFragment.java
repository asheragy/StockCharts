package org.cerion.stockcharts.charts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import org.cerion.stocklist.model.FunctionId;

public class OverlaysDialogFragment extends DialogFragment {

    public interface OnSelectListener {
        void select(Overlay overlay);
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

        arrayAdapter.add("EMA"); //TODO order by define values
        arrayAdapter.add("SMA");
        arrayAdapter.add("BB");
        arrayAdapter.add("KAMA");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Overlay result = null;
                        switch(which)
                        {
                            case Overlay.TYPE_EMA:
                                result = Overlay.getEMA(20);
                                break;
                            case Overlay.TYPE_SMA:
                                result = Overlay.getSMA(20);
                                break;
                            case Overlay.TYPE_BB:
                                result = Overlay.getBB(20, 2.0f);
                                break;
                            case Overlay.TYPE_KAMA:
                                result = Overlay.getKAMA(10, 2, 30);
                        }

                        listener.select(result);
                        dialog.dismiss();
                    }
                })
                .create();
    }
}
