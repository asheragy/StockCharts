package org.cerion.stockcharts.model;

import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.model.Dividend;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.model.Quote;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Position {

    // Required fields
    private final double count;
    private String symbol;
    private double origPrice;
    private Date date;
    private boolean dividendsReinvested;
    private double origPriceAdjusted;

    // Optional
    @Deprecated
    private double currPrice;

    private Quote quote;
    private float totalDividends;
    private Date nextDividendEstimate;
    //private Dividend lastDividend; // TODO make this serializable
    private Date lastDividendDate;
    private double lastDividendAmount;

    private static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");

    public Position(String symbol, double count, double price, Date date)
    {
        this(symbol, count, price, date, false);
    }

    public Position(String symbol, double count, double price, Date date, boolean dividendsReinvested)
    {
        this.symbol = symbol;
        this.count = count;
        this.origPrice = price;
        this.date = date;
        this.dividendsReinvested = dividendsReinvested;
        this.origPriceAdjusted = price;
    }

    public String getSymbol() { return symbol; }

    public double getCount() {
        return count;
    }

    public double getOrigPrice() { return origPrice; }

    public Date getDate() { return date; }

    public boolean IsDividendsReinvested() { return dividendsReinvested; }

    public double getCurrPrice() { return currPrice; }

    public void setCurrPrice(double price) { currPrice = price; }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }

    public void setPriceHistory(PriceList list) {
        if(list.getInterval() == Interval.DAILY) {

            if(IsDividendsReinvested()) {
                for(int i = 0; i < list.size(); i++) {
                    Date date = list.mDate[i];
                    if(dateEquals(date,this.date)) {
                        origPriceAdjusted = list.close(i);
                    }
                }
            }

        } else {
            throw new IllegalArgumentException("daily prices required");
        }
    }

    /**
     * Gets percent difference between current and original/purchase price
     * @return percent difference
     */
    public double getPercentChanged() {
        if(IsDividendsReinvested())
            return 100 * (currPrice - origPriceAdjusted) / origPriceAdjusted;

        return 100 * (currPrice - origPrice) / origPrice;
    }

    /**
     * Gets the percent difference between current price and yesterdays price
     * @return percent difference
     */
    public double getOneDayPercentChange() {
        return quote.changePercent;
    }

    /**
     * Gets the difference between current price and yesterdays price
     * @return price difference
     */
    public double getOneDayChange() {
        return quote.change;
    }

    /**
     * Gets the difference between current price and purchase price
     * @return price difference
     */
    public double getChange() {
        if(IsDividendsReinvested()) {
            double percent = getPercentChanged();
            return origPrice * (percent / 100);
        }

        return quote.getCurrentPrice() - origPrice;
    }

    /**
     * Gets percent difference between current/original price including dividends
     * @return percent difference
     */
    public double getPercentChangedWithDividends() {
        double origValue = getOrigValue();
        double currValueDiv = getCurrValue() + getDividendProfit();
        return 100 * (currValueDiv - origValue) / origValue;
    }

    /**
     * Gets total dividends earned in dollars
     * @return amount earned
     */
    public double getDividendProfit() {
        if(IsDividendsReinvested())
            return 0;

        return totalDividends * count;
    }

    /**
     * Get price change between original and current price in dollars
     * @return profit in dollars
     */
    public double getProfit() {
        if(IsDividendsReinvested()) {
            return getOrigValue() * (getPercentChanged() / 100);
        }
        return getCurrValue() - getOrigValue();
    }

    /**
     * Original value/cost in dollars of all shares
     * @return cost of original lot
     */
    public double getOrigValue() {
        return origPrice * count;
    }

    /**
     * Current value/cost in dollars of all shares
     * @return cost of current lot
     */
    public double getCurrValue() {
        if(IsDividendsReinvested()) {
            return getOrigValue() + getProfit();
        }

        return currPrice * count;
    }

    /**
     * Add dividends earned to the value of this position
     * @param list Historical list of dividends
     */
    public void addDividends(List<Dividend> list) {
        totalDividends = 0;
        Dividend lastDividend = null;
        Dividend secondToLast = null;

        if(list == null || list.size() == 0)
            return;

        //Sort in descending order so most recent dividend is first
        Collections.sort(list, new Comparator<Dividend>() {
            @Override
            public int compare(Dividend lhs, Dividend rhs) {
                return rhs.mDate.compareTo(lhs.mDate);
            }
        });

        // Get total dividend amount
        for(Dividend d : list) {
            if(d.mDate.after(date)) {
                totalDividends += d.mDividend;
            } else
                break;
        }

        lastDividend = list.get(0);
        this.lastDividendAmount = lastDividend.mDividend;
        this.lastDividendDate = lastDividend.mDate;


        // Get 2nd to last dividend
        for(Dividend d : list) {
            if(d.mDate.before(lastDividend.mDate)) {
                if(secondToLast == null || d.mDate.after(secondToLast.mDate))
                    secondToLast = d;
            }
        }

        // Get difference
        if(secondToLast != null) {
            long diff = lastDividend.mDate.getTime() - secondToLast.mDate.getTime();

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(lastDividend.mDate.getTime() + diff);

            nextDividendEstimate = cal.getTime();
        }
    }

    /**
     * Amount in dollars of last dividend paid for share size
     * @return last dividend paid
     */
    public double getLastDividendPaid() { return lastDividendAmount * count; }
    /**
     * Gets the date of last dividend
     * @return Date, or null if no dividends
     */
    public Date getLastDividendDate() { return lastDividendDate; }

    /**
     * Gets the estimated date of the next dividend based on last 2 dividends
     * @return estimated Date
     */
    public Date getNextDividendEstimate() { return nextDividendEstimate; }

    @Override
    public String toString()
    {
        return symbol + " " + count + "@" + origPrice;
    }

    private boolean dateEquals(Date d1, Date d2) {
        return dayFormat.format(d1).equals(dayFormat.format(d2));
    }
}
