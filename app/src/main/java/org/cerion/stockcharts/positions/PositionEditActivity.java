package org.cerion.stockcharts.positions;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.DatePickerFragment;
import org.cerion.stockcharts.common.ViewModelActivity;
import org.cerion.stockcharts.databinding.ActivityPositionEditBinding;
import org.cerion.stocks.core.model.PositionWithDividends;

import java.util.Date;

public class PositionEditActivity extends ViewModelActivity<PositionEditViewModel>
        implements DatePickerFragment.OnDateSetListener,
        PositionEditViewModel.IView
{
    public static final String TAG = PositionEditActivity.class.getSimpleName();
    private static final String EXTRA_POSITION_ID = "id";

    public static Intent newIntent(Context context, PositionWithDividends p) {
        Intent intent = new Intent(context, PositionEditActivity.class);
        intent.putExtra(EXTRA_POSITION_ID, p.getId());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityPositionEditBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_position_edit);
        binding.setViewModel(getViewModel());

        if (!isRetained() && getIntent().hasExtra(EXTRA_POSITION_ID)) {
            int id = getIntent().getIntExtra(EXTRA_POSITION_ID, 0);
            getViewModel().setPosition(id);
        }
    }

    @Override
    public void onSelectDate() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(Date date) {
        getViewModel().setDate(date);
    }

    @Override
    public void onFinish() {
        finish();
    }

    @Override
    public void onError(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected PositionEditViewModel newViewModel() {
        return new PositionEditViewModel(this, this);
    }
}
