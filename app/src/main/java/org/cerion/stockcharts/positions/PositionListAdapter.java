package org.cerion.stockcharts.positions;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.cerion.stockcharts.R;
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
        TextView oneday_change;
        TextView total_change;

        TextView profit;
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
            viewHolder.oneday_change = (TextView) convertView.findViewById(R.id.oneday_change);
            viewHolder.total_change = (TextView) convertView.findViewById(R.id.total_change);

            viewHolder.profit = (TextView) convertView.findViewById(R.id.profit);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.symbol.setText(p.getSymbol());
        viewHolder.purchase_date.setText( Utils.dateFormatShort.format(p.getDate()) );
        viewHolder.purchase_lot.setText(Utils.getDecimalFormat3(p.getCount()) + " @ " + df.format(p.getOrigPrice()));

        if(p.getCurrPrice() > 0) {

            // Current price
            viewHolder.current_price.setText( df.format(p.getCurrPrice()) );

            // Change since previous day
            String sign = (p.getOneDayChange() > 0 ? "+" : "");
            viewHolder.oneday_change.setText( sign + df.format(p.getOneDayChange()) + " (" + df.format(p.getOneDayPercentChange()) + "%)");

            // Total Change since purchsae
            sign = (p.getPercentChanged() > 0 ? "+" : "");
            viewHolder.total_change.setText( sign + df.format(p.getChange()) + " (" + df.format(p.getPercentChanged()) + "%)");

            // Profit
            double profit = p.getProfit();
            double dividendProfit = p.getDividendProfit();
            String profit_str = "$" + df.format(profit);
            if(dividendProfit > 0)
                profit_str += " (+" + df.format(dividendProfit) + ")";
            viewHolder.profit.setText( profit_str );

            //Color
            setColor(viewHolder.oneday_change, p.getOneDayChange());
            setColor(viewHolder.total_change, p.getPercentChanged());
            //setColor(viewHolder.profit, profit);
        } else {
            viewHolder.current_price.setText("...");
            viewHolder.oneday_change.setText("");
            //viewHolder.profit.setText("");
        }

        return convertView;
    }

    private void setColor(TextView tv, double diff) {
        if(diff == 0)
            tv.setTextColor(Color.BLACK);
        else
            tv.setTextColor(diff > 0 ? mColorGreen : mColorRed);
    }
}