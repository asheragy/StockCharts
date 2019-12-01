package org.cerion.stockcharts.charts;

import android.databinding.ObservableField;
import android.graphics.Color;
import android.os.AsyncTask;

import org.cerion.stockcharts.common.Utils;
import org.cerion.stocks.core.model.Quote;
import org.cerion.stocks.core.web.DataAPI;

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

    private class InfoTask extends AsyncTask<Void, Void, Quote> {

        private String symbol;
        public InfoTask(String symbol) {
            this.symbol = symbol;
        }

        @Override
        protected Quote doInBackground(Void... params) {
            Quote q = api.getQuote(symbol);
            return q;
        }

        @Override
        protected void onPostExecute(Quote quote) {
            if (quote == null)
                return;

            fullName.set(quote.getName());
            symbolExchange.set(quote.getExchange() + ": " + quote.getSymbol());
            currentPrice.set("" + quote.getLastTrade());

            String changeStr = Math.abs(quote.getChange()) + " (" + Math.abs(quote.getChangePercent()) + "%)";
            if (quote.getChange() > 0)
                changeStr = "▲" + changeStr;
            else if (quote.getChange() < 0)
                changeStr = "▼" + changeStr;
            priceChange.set(changeStr);

            if (quote.getChange() > 0)
                change.set(1);
            else if (quote.getChange() < 0)
                change.set(-1);

            marketCap.set(quote.getMarketCap());
            peRatio.set(getString(quote.getPeRatio()));
            yield.set("" + quote.getDividendYield());
            beta.set(getString(quote.getBeta()));
            sector.set("" + quote.getSector());
            eps.set("" + quote.getEps());

            if (quote.getVolume() > 1000000) {
                double v = quote.getVolume();
                v /= 1000000;
                volume.set(Utils.decimalFormat.format(v));
            }
            else
                volume.set("" + quote.getVolume());

            yearRange.set( Utils.highLowRange(quote.getLastTrade(), quote.getHigh52(), quote.getLow52()));
        }
    }

    public void load(final String symbol) {
        InfoTask task = new InfoTask(symbol);
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
