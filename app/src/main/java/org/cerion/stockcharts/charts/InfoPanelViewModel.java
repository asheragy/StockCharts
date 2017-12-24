package org.cerion.stockcharts.charts;

import android.databinding.ObservableField;
import android.graphics.Color;
import android.os.AsyncTask;

import org.cerion.stockcharts.common.Utils;
import org.cerion.stocklist.model.Quote;
import org.cerion.stocklist.web.DataAPI;

public class InfoPanelViewModel {

    public ObservableField<String> fullName = new ObservableField<>("");
    public ObservableField<String> symbolExchange = new ObservableField<>("");
    public ObservableField<String> currentPrice = new ObservableField<>("");
    public ObservableField<String> priceChange = new ObservableField<>("");

    public ObservableField<String> marketCap = new ObservableField<>("");
    public ObservableField<String> peRatio = new ObservableField<>("");
    public ObservableField<String> yield = new ObservableField<>("");
    public ObservableField<String> beta = new ObservableField<>("");
    public ObservableField<String> sector = new ObservableField<>("");
    public ObservableField<String> eps = new ObservableField<>("");
    public ObservableField<String> volume = new ObservableField<>("");
    public ObservableField<Integer> change = new ObservableField<>(0);
    public ObservableField<Integer> yearRange = new ObservableField<>(0);

    private DataAPI api;

    public InfoPanelViewModel(DataAPI api) {
        this.api = api;

    }

    public void load(final String symbol) {
        AsyncTask<Void, Void, Quote>  task = new AsyncTask<Void, Void, Quote>() {
            @Override
            protected Quote doInBackground(Void... params) {
                Quote q = api.getQuote(symbol);
                return q;
            }

            @Override
            protected void onPostExecute(Quote quote) {
                fullName.set(quote.name);
                symbolExchange.set(quote.exchange + ": " + quote.symbol);
                currentPrice.set("" + quote.lastTrade);

                String changeStr = Math.abs(quote.change) + " (" + Math.abs(quote.changePercent) + "%)";
                if (quote.change > 0)
                    changeStr = "▲" + changeStr;
                else if (quote.change < 0)
                    changeStr = "▼" + changeStr;
                priceChange.set(changeStr);

                if (quote.change > 0)
                    change.set(1);
                else if (quote.change < 0)
                    change.set(-1);

                marketCap.set(quote.marketCap);
                peRatio.set(getString(quote.peRatio));
                yield.set("" + quote.dividendYield);
                beta.set(getString(quote.beta));
                sector.set("" + quote.sector);
                eps.set("" + quote.eps);

                if (quote.volume > 1000000) {
                    double v = quote.volume;
                    v /= 1000000;
                    volume.set(Utils.decimalFormat.format(v));
                }
                else
                    volume.set("" + quote.volume);

                yearRange.set( Utils.highLowRange(quote.lastTrade, quote.high52, quote.low52));
            }
        };

        task.execute();
    }

    public int getColor() {
        return Color.GREEN;
    }

    public String getString(float value) {
        if (Float.isNaN(value))
            return "";

        return "" + value;
    }
}
