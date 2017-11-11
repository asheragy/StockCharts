package org.cerion.stockcharts.positions;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import org.cerion.stockcharts.Injection;
import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.ViewModelActivity;
import org.cerion.stockcharts.databinding.ActivityPositionDetailsBinding;

public class PositionDetailActivity extends ViewModelActivity<PositionDetailViewModel> {

    private static final String TAG = PositionDetailActivity.class.getSimpleName();
    private static final String EXTRA_POSITION_ID = "position_id";

    public static Intent newIntent(Context context, int id) {
        Intent intent = new Intent(context, PositionDetailActivity.class);
        intent.putExtra(EXTRA_POSITION_ID, id);
        return intent;
    }

    @Override
    protected PositionDetailViewModel newViewModel() {
        return new PositionDetailViewModel(Injection.getAPI(this), Injection.getPositionRepository(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPositionDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_position_details);

        if(!isRetained()) {
            int id = getIntent().getIntExtra(EXTRA_POSITION_ID, 0);
            getViewModel().load(id);
            getViewModel().update();
        }

        binding.setViewModel(getViewModel());
    }
}
