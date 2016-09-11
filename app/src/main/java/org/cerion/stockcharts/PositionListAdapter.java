package org.cerion.stockcharts;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.cerion.stockcharts.common.Utils;
import org.cerion.stockcharts.model.Position;

import java.text.DecimalFormat;
import java.util.List;

//TODO, update positions in async task from adapter
// - boolean to refresh quotes
// - if not refreshed start async task to update
// - if updated use previous value (on rotate for example)
// - find way to reuse duplicate symbols
public class PositionListAdapter extends ArrayAdapter<Position> {

    private int mColorGreen;
    private int mColorRed;
    private static DecimalFormat df = Utils.decimalFormat;

    private class ViewHolder {
        TextView symbol;
        TextView purchase_lot; // count and price
        TextView purchase_date;

        //With quote data
        TextView current_price;
        TextView price_percent_change;
        TextView profit;
        TextView dividends_earned;
    }

    public PositionListAdapter(Context context, int resource, List<Position> objects) {
        super(context, resource, objects);

        mColorGreen = context.getResources().getColor(R.color.positive_green);
        mColorRed = context.getResources().getColor(R.color.negative_red);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Position p = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_position, parent, false);

            viewHolder.symbol = (TextView) convertView.findViewById(R.id.symbol);
            viewHolder.purchase_lot = (TextView) convertView.findViewById(R.id.purchase_lot);
            viewHolder.purchase_date = (TextView) convertView.findViewById(R.id.purchase_date);
            viewHolder.current_price = (TextView) convertView.findViewById(R.id.current_price);
            viewHolder.price_percent_change = (TextView) convertView.findViewById(R.id.price_percent_change);
            viewHolder.profit = (TextView) convertView.findViewById(R.id.profit);
            viewHolder.dividends_earned = (TextView) convertView.findViewById(R.id.dividends_earned);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.symbol.setText(p.getSymbol());
        viewHolder.purchase_date.setText( Utils.dateFormatShort.format(p.getDate()) );
        viewHolder.purchase_lot.setText(Utils.getDecimalFormat3(p.getCount()) + " @ " + df.format(p.getOrigPrice()));

        if(p.getCurrPrice() > 0) {
            double profit = p.getProfit();

            viewHolder.current_price.setText( df.format(p.getCurrPrice()) );
            viewHolder.price_percent_change.setText( df.format(p.getPercentChanged()) + "%");
            viewHolder.profit.setText( df.format(p.getProfit()) );

            //Color
            setColor(viewHolder.current_price, p.getPercentChanged());
            setColor(viewHolder.profit, profit);
            setColor(viewHolder.price_percent_change, profit);
        } else {
            viewHolder.current_price.setText("...");
            viewHolder.price_percent_change.setText("");
            viewHolder.profit.setText("");
        }

        double dividendProfit = p.getDividendProfit();
        if(dividendProfit > 0)
            viewHolder.dividends_earned.setText("$" + df.format(p.getDividendProfit()));
        else
            viewHolder.dividends_earned.setText("");

        return convertView;
    }

    private void setColor(TextView tv, double diff) {
        if(diff == 0)
            tv.setTextColor(Color.BLACK);
        else
            tv.setTextColor(diff > 0 ? mColorGreen : mColorRed);
    }
}