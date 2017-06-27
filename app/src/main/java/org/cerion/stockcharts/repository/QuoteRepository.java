package org.cerion.stockcharts.repository;

import android.content.Context;

import org.cerion.stocklist.model.Quote;

public class QuoteRepository extends SQLiteRepositoryBase {

    public QuoteRepository(Context context) {
        super(context);
    }

    public Quote get(String symbol) {
        return mYahooFinance.getQuote(symbol);
    }
}
