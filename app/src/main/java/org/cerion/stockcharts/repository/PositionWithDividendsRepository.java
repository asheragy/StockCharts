package org.cerion.stockcharts.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.cerion.stockcharts.database.StockDBOpenHelper;
import org.cerion.stockcharts.database.Tables.Positions;
import org.cerion.stocks.core.model.PositionWithDividends;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PositionWithDividendsRepository extends SQLiteRepositoryBase {

    public PositionWithDividendsRepository(Context context) {
        super(StockDBOpenHelper.getInstance(context));
    }

    public void add(PositionWithDividends position) {
        ContentValues values = getContentValues(position);
        insert(Positions.TABLE_NAME, values);
    }

    public void update(PositionWithDividends position) {
        ContentValues values = getContentValues(position);
        update(Positions.TABLE_NAME, values, Positions._ID, position.getId());
    }

    public List<PositionWithDividends> getAll() {
        Cursor c = db.query(Positions.TABLE_NAME, null, null, null, null, null, Positions._DATE); //order by date
        List<PositionWithDividends> result = new ArrayList<>();

        if (c != null) {
            while (c.moveToNext()) {
                PositionWithDividends p = fromCursor(c);
                result.add(p);
            }
            c.close();
        }

        return result;
    }

    // TODO change to use ID
    public void delete(PositionWithDividends position) {
        // TODO refine better or use autoID
        delete(Positions.TABLE_NAME, String.format("%s='%s' AND %s=%s", Positions._SYMBOL, position.getSymbol(), Positions._DATE, position.getDate().getTime()) );

    }

    public PositionWithDividends get(int id) {
        String where = String.format(Positions._ID + "=%d", id);
        Cursor c = db.query(Positions.TABLE_NAME, null, where, null, null, null, null);
        PositionWithDividends result = null;

        if(c != null) {
            if (c.moveToNext()) {
                result = fromCursor(c);
            }

            c.close();
        }

        return result;
    }

    private ContentValues getContentValues(PositionWithDividends position) {
        ContentValues values = new ContentValues();
        values.put(Positions._SYMBOL, position.getSymbol());
        values.put(Positions._DATE, position.getDate().getTime());
        values.put(Positions._PRICE, position.getOrigPrice());
        values.put(Positions._COUNT, position.getCount());
        values.put(Positions._DR, position.getDividendsReinvested());
        values.put(Positions._ACCOUNTID, position.getAccountId());

        return values;
    }

    private PositionWithDividends fromCursor(Cursor c) {
        String symbol = c.getString(c.getColumnIndexOrThrow(Positions._SYMBOL));
        double count = c.getDouble(c.getColumnIndexOrThrow(Positions._COUNT));
        double price = c.getDouble(c.getColumnIndexOrThrow(Positions._PRICE));
        long date = c.getLong(c.getColumnIndexOrThrow(Positions._DATE));
        boolean dr = c.getInt(c.getColumnIndexOrThrow(Positions._DR)) != 0;

        PositionWithDividends result = new PositionWithDividends(symbol, count, price, new Date(date), dr);
        result.setId( c.getInt(c.getColumnIndexOrThrow(Positions._ID)) );
        result.setAccountId( c.getInt(c.getColumnIndexOrThrow(Positions._ACCOUNTID)));

        return result;
    }
}