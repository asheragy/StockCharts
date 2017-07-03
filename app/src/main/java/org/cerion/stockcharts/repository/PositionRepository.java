package org.cerion.stockcharts.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.cerion.stockcharts.database.StockDBOpenHelper;
import org.cerion.stocklist.model.Position;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PositionRepository extends SQLiteRepositoryBase {

    public PositionRepository(Context context) {
        super(StockDBOpenHelper.getInstance(context));
    }

    public void add(Position position) {
        ContentValues values = new ContentValues();
        values.put(StockDBOpenHelper.Positions._SYMBOL, position.getSymbol());
        values.put(StockDBOpenHelper.Positions._DATE, position.getDate().getTime());
        values.put(StockDBOpenHelper.Positions._PRICE, position.getOrigPrice());
        values.put(StockDBOpenHelper.Positions._COUNT, position.getCount());
        values.put(StockDBOpenHelper.Positions._DR, position.IsDividendsReinvested());

        insert(StockDBOpenHelper.Positions.TABLE_NAME, values);
    }

    public void update(Position position) {
        ContentValues values = new ContentValues();
        values.put(StockDBOpenHelper.Positions._SYMBOL, position.getSymbol());
        values.put(StockDBOpenHelper.Positions._DATE, position.getDate().getTime());
        values.put(StockDBOpenHelper.Positions._PRICE, position.getOrigPrice());
        values.put(StockDBOpenHelper.Positions._COUNT, position.getCount());
        values.put(StockDBOpenHelper.Positions._DR, position.IsDividendsReinvested());

        update(StockDBOpenHelper.Positions.TABLE_NAME, values, StockDBOpenHelper.Positions._ID, position.getId());
    }

    public List<Position> getAll() {
        SQLiteDatabase db = openReadOnly();

        Cursor c = db.query(StockDBOpenHelper.Positions.TABLE_NAME, null, null, null, null, null, StockDBOpenHelper.Positions._DATE); //order by date
        List<Position> result = new ArrayList<>();

        if (c != null) {
            while (c.moveToNext()) {
                Position p = fromCursor(c);
                result.add(p);
            }
            c.close();
        }

        db.close();
        return result;
    }

    // TODO change to use ID
    public void delete(Position position) {
        // TODO refine better or use autoID
        delete(StockDBOpenHelper.Positions.TABLE_NAME, String.format("%s='%s' AND %s=%s", StockDBOpenHelper.Positions._SYMBOL, position.getSymbol(), StockDBOpenHelper.Positions._DATE, position.getDate().getTime()) );

    }

    public Position get(int id) {
        SQLiteDatabase db = openReadOnly();

        String where = String.format(StockDBOpenHelper.Positions._ID + "=%d", id);
        Cursor c = db.query(StockDBOpenHelper.Positions.TABLE_NAME, null, where, null, null, null, null);
        Position result = null;

        if(c != null) {
            if (c.moveToNext()) {
                result = fromCursor(c);
            }

            c.close();
        }

        db.close();
        return result;
    }

    private Position fromCursor(Cursor c) {
        String symbol = c.getString(c.getColumnIndexOrThrow(StockDBOpenHelper.Positions._SYMBOL));
        double count = c.getDouble(c.getColumnIndexOrThrow(StockDBOpenHelper.Positions._COUNT));
        double price = c.getDouble(c.getColumnIndexOrThrow(StockDBOpenHelper.Positions._PRICE));
        long date = c.getLong(c.getColumnIndexOrThrow(StockDBOpenHelper.Positions._DATE));
        boolean dr = c.getInt(c.getColumnIndexOrThrow(StockDBOpenHelper.Positions._DR)) != 0;

        Position result = new Position(symbol, count, price, new Date(date), dr);
        result.setId( c.getInt(c.getColumnIndexOrThrow(StockDBOpenHelper.Positions._ID)) );

        return result;
    }
}
