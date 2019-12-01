package org.cerion.stockcharts.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.cerion.stockcharts.database.StockDBOpenHelper;
import org.cerion.stocks.core.model.Symbol;

import java.util.ArrayList;
import java.util.List;

public class SymbolRepository extends SQLiteRepositoryBase {

    public SymbolRepository(Context context) {
        super(StockDBOpenHelper.getInstance(context));
    }

    public List<Symbol> getAll() {
        Cursor c = db.query(StockDBOpenHelper.Symbols.TABLE_NAME, null, null, null, null, null, StockDBOpenHelper.Symbols._SYMBOL);
        List<Symbol> result = new ArrayList<>();

        if (c != null) {
            while (c.moveToNext()) {
                String symbol = c.getString(c.getColumnIndexOrThrow(StockDBOpenHelper.Symbols._SYMBOL));
                String name = c.getString(c.getColumnIndexOrThrow(StockDBOpenHelper.Symbols._NAME));
                String exchange = c.getString(c.getColumnIndexOrThrow(StockDBOpenHelper.Symbols._EXCHANGE));
                result.add(new Symbol(symbol, name, exchange));
            }
            c.close();
        }

        return result;
    }

    public void add(Symbol symbol) {
        ContentValues values = new ContentValues();
        values.put(StockDBOpenHelper.Symbols._SYMBOL, symbol.getSymbol());
        values.put(StockDBOpenHelper.Symbols._NAME, symbol.getName());
        values.put(StockDBOpenHelper.Symbols._EXCHANGE, symbol.getExchange());

        db.insertWithOnConflict(StockDBOpenHelper.Symbols.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void delete(String symbol) {
        String where = String.format(StockDBOpenHelper.Symbols._SYMBOL + "='%s'", symbol);
        delete(StockDBOpenHelper.Symbols.TABLE_NAME, where);
    }

        /*
    @Override
    public void deleteAllSymbols() {
        delete(Symbols.TABLE_NAME, String.format("%s!='%s'", Symbols._NAME, ".") );
    }
    */
}
