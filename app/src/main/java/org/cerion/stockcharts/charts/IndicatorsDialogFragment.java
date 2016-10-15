package org.cerion.stockcharts.charts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import org.cerion.stockcharts.R;
import org.cerion.stocklist.model.FunctionId;

public class IndicatorsDialogFragment extends DialogFragment {

    public interface OnSelectListener {
        void select(FunctionId id);
    }

    public static IndicatorsDialogFragment newInstance(int title) {
        IndicatorsDialogFragment frag = new IndicatorsDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");

        final OnSelectListener listener = (OnSelectListener)getActivity();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.select_dialog_item);


        FunctionId[] ids = FunctionId.values();
        arrayAdapter.add("Price");
        for(FunctionId id : ids)
        {
            arrayAdapter.add(id.toString());
        }

        return new AlertDialog.Builder(getActivity())
                //.setIcon(R.drawable.alert_dialog_icon)
                .setTitle(title)

                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which == 0) {
                            listener.select(null);
                        } else {
                            FunctionId selected = FunctionId.values()[which - 1];
                            listener.select(selected);
                        }
                        dialog.dismiss();
                    }
                })
                /*
                .setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((FragmentAlertDialog)getActivity()).doPositiveClick();
                            }
                        }
                )
                .setNegativeButton(R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((FragmentAlertDialog)getActivity()).doNegativeClick();
                            }
                        }
                )
                */
                .create();
    }

}
