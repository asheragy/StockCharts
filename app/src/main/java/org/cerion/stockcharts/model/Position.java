package org.cerion.stockcharts.model;

import org.cerion.stocklist.model.Dividend;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Position implements Serializable {

    // Required fields
    private final double count;
    private String symbol;
    private double origPrice;
    private Date date;
    private boolean dividendsReinvested;

    // Optional
    private double currPrice;
    private float totalDividends;
    private Date nextDividendEstimate;
    //private Dividend lastDividend; // TODO make this serializable
    private Date lastDividendDate;
    private double lastDividendAmount;

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

    /**
     * Gets percent difference between current and original price
     * @return percent difference
     */
    public double getPercentChanged() {
        return 100 * (currPrice - origPrice) / origPrice;
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
        return totalDividends * count;
    }

    /**
     * Get price change between original and current price in dollars
     * @return profit in dollars
     */
    public double getProfit() {
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
}
