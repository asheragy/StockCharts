package org.cerion.stockcharts.positions;

import android.databinding.ObservableField;

import org.cerion.stockcharts.common.Constants;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.common.Utils;
import org.cerion.stockcharts.repository.PositionRepository;
import org.cerion.stocks.core.PriceList;
import org.cerion.stocks.core.model.Dividend;
import org.cerion.stocks.core.model.DividendHistory;
import org.cerion.stocks.core.model.Interval;
import org.cerion.stocks.core.model.Position;
import org.cerion.stocks.core.model.PositionValue;
import org.cerion.stocks.core.web.DataAPI;

import java.text.DecimalFormat;
import java.util.List;

public class PositionDetailViewModel {

    private Position purchase;
    private PositionRepository repo;
    private DataAPI api;
    private static DecimalFormat df = new DecimalFormat("0.00");

    public ObservableField<String> currentCount = new ObservableField<>();
    public ObservableField<String> currentPrice = new ObservableField<>();
    public ObservableField<String> currentValue = new ObservableField<>();

    public ObservableField<String> dividends = new ObservableField<>();
    public ObservableField<String> lastDividendPaid = new ObservableField<>();
    public ObservableField<String> lastDividendDate = new ObservableField<>();
    public ObservableField<String> nextDividendEst = new ObservableField<>();

    public ObservableField<String> change = new ObservableField<>();
    public ObservableField<String> profit = new ObservableField<>();

    public ObservableField<String> changeWithDividends = new ObservableField<>();
    public ObservableField<String> profitWithDividends = new ObservableField<>();

    public ObservableField<Boolean> showDividendFields = new ObservableField<>();

    public PositionDetailViewModel(DataAPI api, PositionRepository repo) {
        this.api = api;
        this.repo = repo;
    }

    public void load(int id) {
        purchase = repo.get(id);
    }

    public String getSymbol() {
        return purchase.getSymbol();
    }

    public String getCount() {
        return Utils.getDecimalFormat3(purchase.getCount());
    }

    public String getPurchasePrice() {
        return Utils.decimalFormat.format(purchase.getOrigPrice());
    }

    public String getPurchaseDate() {
        return Utils.dateFormatLong.format(purchase.getDate());
    }

    public String getPurchaseCost() {
        return "$" + Utils.decimalFormat.format(purchase.getOrigValue());
    }

    public String getDividendsReinvested() {
        return purchase.getDividendsReinvested() ? "Yes" : "No";
    }

    public void update() {
        new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            @Override
            public void run() {
                final String symbol = purchase.getSymbol();
                List<Dividend> dividendList = api.getDividends(symbol);
                PriceList list;

                try {
                    list = new PriceList(symbol, api.getPrices(symbol, Interval.DAILY, Constants.START_DATE_DAILY));
                } catch (Exception e) {
                    // TODO set some error state
                    return;
                }

                PositionValue position = new PositionValue(purchase, list);
                position.addDividends(dividendList);

                currentPrice.set( df.format(position.getCurrPrice()) );
                currentValue.set("$" + df.format(position.getCurrValue()));
                currentCount.set( Utils.getDecimalFormat3(position.getCurrCount())); // TODO hide if no dividends reinvested

                change.set(df.format(position.getPercentChanged()) + "%");
                profit.set("$" + df.format(position.getProfit()));
                dividends.set("$" + df.format(position.getDividendProfit()));

                DividendHistory hist = position.getDividendHistory();
                if (dividends != null) {
                    lastDividendPaid.set( df.format(hist.getLastDividend()));
                    lastDividendDate.set( Utils.dateFormatLong.format(hist.getLastDividendDate()) );
                    nextDividendEst.set( Utils.dateFormatLong.format(hist.getNextDividendEstimate()) );
                }

                changeWithDividends.set(df.format(position.getPercentChangedWithDividends()) + "%");
                profitWithDividends.set("$" + df.format(position.getProfit() + position.getDividendProfit()));

                // If dividends are reinvested they are treated as part of the normal stock changes so don't show extra info on them
                if (purchase.getDividendsReinvested())
                    showDividendFields.set(false);
                else
                    showDividendFields.set(true);
            }

            @Override
            public void onFinish() {
            }

        }).execute();
    }
}
