package org.cerion.stockcharts.positions;

import android.databinding.ObservableField;

import org.cerion.stockcharts.common.Utils;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.model.Dividend;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.model.PositionValue;

import java.text.DecimalFormat;
import java.util.List;

public class PositionItemViewModel {

    private Position mPosition;
    private DecimalFormat df = Utils.decimalFormat;

    public ObservableField<Float> dayChange = new ObservableField<>(0.0f);
    public ObservableField<Float> totalChange = new ObservableField<>(0.0f);
    public ObservableField<String> dayChangeStr = new ObservableField<>("0.00 (0.00%)");
    public ObservableField<String> totalChangeStr = new ObservableField<>("0.00 (0.00%)");
    public ObservableField<String> currentPrice = new ObservableField<>("...");
    public ObservableField<String> dividendProfit = new ObservableField<>("");

    PositionItemViewModel(Position p) {
        mPosition = p;
    }

    public void setData(PriceList list, List<Dividend> dividends) {
        PositionValue value = new PositionValue(mPosition, list);
        value.addDividends(dividends);

        float change = list.getChange();
        float percent = list.getPercentChange() * 100;
        String sign = (change > 0 ? "+" : "");
        String format = "%s%s (%s%%)";
        dayChangeStr.set(String.format(format, sign, df.format(change), df.format(percent)));
        dayChange.set(change);

        // TODO possibly only percent should go here, with dividend reinvested increase is price and/or shares increasing
        // This should actually just be the $ amount gain/loss ignoring dividends
        // Total change
        float profit = (float)value.getProfit();
        sign = (profit > 0 ? "+" : "");
        format = "%s$%s (%s%%)";
        format = String.format(format, sign, df.format(profit), df.format(value.getPercentChanged() * 100) );
        totalChangeStr.set(format);
        totalChange.set(profit);

        // Current Price
        currentPrice.set( df.format(value.getCurrPrice()) );

        // Dividends
        profit = (float)value.getDividendProfit();
        if(profit > 0) {
            dividendProfit.set("$" + df.format(profit));
        } else
            dividendProfit.set("");
    }

    public Position getPosition() {
        return mPosition;
    }

    public String symbol() {
        return mPosition.getSymbol();
    }

    public String purchaseDate() {
        return Utils.dateFormatShort.format(mPosition.getDate());
    }

    // TODO this needs to be dynamic
    public String purchaseLot() {
        return Utils.getDecimalFormat3(mPosition.getCount()) + " @ " + df.format(mPosition.getOrigPrice());
    }
}
