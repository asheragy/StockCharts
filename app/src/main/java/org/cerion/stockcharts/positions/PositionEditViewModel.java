package org.cerion.stockcharts.positions;

import android.content.Context;
import android.databinding.ObservableField;
import android.util.Log;
import android.widget.Toast;

import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.common.Utils;
import org.cerion.stockcharts.repository.PositionRepository;
import org.cerion.stocks.core.model.Position;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PositionEditViewModel {
    private static final String TAG = PositionEditViewModel.class.getSimpleName();

    private Context mContext;
    private IView mListener;
    private PositionRepository repo;
    private int mId = 0;

    public final ObservableField<String> symbol = new ObservableField<>("");
    public final ObservableField<String> count = new ObservableField<>("");
    public final ObservableField<String> price = new ObservableField<>("");
    public final ObservableField<Boolean> dividendsReinvested = new ObservableField<>(false);
    public final ObservableField<Date> date = new ObservableField<>();

    public List<String> accounts = new ArrayList<>();
    public final ObservableField<Integer> accountIndex = new ObservableField<>();

    public interface IView {
        void onFinish();
        void onError(Exception e);
        void onSelectDate();
    }

    public PositionEditViewModel(Context context, IView listener) {
        mContext = context;
        mListener = listener;
        repo = new PositionRepository(context);

        accounts.add("Account 1");
        accounts.add("Account 2");
        accounts.add("Account 3");
        accounts.add("Account 4");
        accounts.add("Account 5");
    }

    public void setPosition(int id) {
        mId = id;
        Position position = repo.get(id);
        setPosition(position);
    }

    private void setPosition(Position position) {
        date.set(position.getDate());
        symbol.set(position.getSymbol());
        count.set( Utils.getDecimalFormat3(position.getCount()) );
        price.set( Utils.getDecimalFormat3(position.getOrigPrice()) );
        dividendsReinvested.set(position.getDividendsReinvested());
        accountIndex.set(position.getAccountId());
    }

    public void selectDate() {
        mListener.onSelectDate();
    }

    public void setDate(Date newDate) {
        date.set(newDate);
    }

    public String convertDateLabel(Date date) {
        if (date == null)
            return "Date";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(date);
    }

    public void cancel() {
        mListener.onFinish();
    }

    public void save() {
        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            Exception ex = null;

            @Override
            public void run() {
                try {
                    double d_count = Double.parseDouble(count.get());
                    double d_price = Double.parseDouble(price.get());

                    Position p = new Position(symbol.get(), d_count, d_price, date.get(), dividendsReinvested.get());
                    p.setAccountId(accountIndex.get());

                    String log = p.getSymbol() + "\t" + p.getCount() + "\t" + p.getOrigPrice() + "\t" + p.getDate() + "\t" + p.getAccountId();
                    if (mId > 0) {
                        Log.d(TAG, "UPDATE position " + log);
                        p.setId(mId);
                        repo.update(p);
                    } else {
                        Log.d(TAG, "ADD position " + log);
                        repo.add(p);
                    }

                } catch (Exception e) {
                    ex = e;
                }
            }

            @Override
            public void onFinish() {
                if (ex == null) {
                    Toast.makeText(mContext, "Saved " + symbol.get(), Toast.LENGTH_SHORT).show();
                    mListener.onFinish();
                } else {
                    mListener.onError(ex);
                }

            }
        });

        task.execute();
    }
}
