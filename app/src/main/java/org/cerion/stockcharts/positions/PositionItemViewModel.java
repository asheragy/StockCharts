package org.cerion.stockcharts.positions;

import android.graphics.Color;

import org.cerion.stockcharts.common.Utils;
import org.cerion.stocklist.model.Position;

import java.text.DecimalFormat;

public class PositionItemViewModel {

    private Position mPosition;
    private DecimalFormat df = Utils.decimalFormat;
    private int mColorGreen;
    private int mColorRed;

    // TODO add dividends re-invested
    // 1. Calculate percent gain (current_close - purchase_date_close) / purchase_Date_close
    // 2. Apply percent increase to purchase cost

    PositionItemViewModel(Position p, int positiveColor, int negativeColor) {
        mPosition = p;
        mColorGreen = positiveColor;
        mColorRed = negativeColor;
    }

    public String symbol() {
        return mPosition.getSymbol();
    }

    public String purchaseDate() {
        return Utils.dateFormatShort.format(mPosition.getDate());
    }

    public String purchaseLot() {
        return Utils.getDecimalFormat3(mPosition.getCount()) + " @ " + df.format(mPosition.getOrigPrice());
    }

    public String currentPrice() {
        if (hasQuote())
            return df.format(mPosition.getCurrPrice());
        else
            return "...";
    }

    public String dayChange() {
        if (hasQuote()) {
            String sign = (mPosition.getOneDayChange() > 0 ? "+" : "");
            String format = "%s%s (%s%%)";
            return String.format(format, sign, df.format(mPosition.getOneDayChange()), df.format(mPosition.getOneDayPercentChange()));
        }

        return "";
    }

    public String totalChange() {
        if (hasQuote()) {
            String sign = (mPosition.getPercentChanged() > 0 ? "+" : "");
            String format = "%s%s (%s%%)";
            return String.format(format, sign, df.format(mPosition.getChange()), df.format(mPosition.getPercentChanged()) );
        }

        return "";
    }

    public int dayChangeColor() {
        if (hasQuote())
            return getColor( mPosition.getOneDayChange() );

        return 0;
    }

    public int totalChangeColor() {
        if (hasQuote())
            return getColor( mPosition.getPercentChanged() );

        return 0;
    }

    public String profit() {
        if (hasQuote()) {
            double profit = mPosition.getProfit();
            double dividendProfit = mPosition.getDividendProfit();
            String profit_str = "$" + df.format(profit);
            if(dividendProfit > 0)
                profit_str += " (+" + df.format(dividendProfit) + ")";

            return profit_str;
        }

        return "";
    }

    private boolean hasQuote() {
        return mPosition.getCurrPrice() > 0;
    }

    private int getColor(double diff) {
        if(diff == 0)
            return Color.BLACK;
        else
            return diff > 0 ? mColorGreen : mColorRed;
    }
}
