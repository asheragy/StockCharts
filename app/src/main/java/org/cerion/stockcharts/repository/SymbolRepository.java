package org.cerion.stockcharts.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.cerion.stockcharts.database.StockDBOpenHelper;
import org.cerion.stocklist.model.Symbol;
import org.cerion.stocklist.web.DataAPI;
import org.cerion.stocklist.web.YahooFinance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SymbolRepository extends SQLiteRepositoryBase {

    private DataAPI mYahooFinance = YahooFinance.getInstance();

    public SymbolRepository(Context context) {
        super(StockDBOpenHelper.getInstance(context));
    }

    public List<Symbol> getAll() {
        SQLiteDatabase db = openReadOnly();

        Cursor c = db.query(StockDBOpenHelper.Symbols.TABLE_NAME, null, null, null, null, null, StockDBOpenHelper.Symbols._NAME); //order by name
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

        db.close();
        return result;
    }

    public boolean add(String symbol) {
        // TODO For now just always update, look into optimizing

        Set<String> arr = new HashSet<>(Arrays.asList(symbol));
        List<Symbol> s = mYahooFinance.getSymbols(arr);
        if(s.size() > 0 && s.get(0) != null) {
            add(s.get(0));
            return true;
        }

        return false;
    }

    public void add(Symbol symbol) {
        SQLiteDatabase db = open();

        ContentValues values = new ContentValues();
        values.put(StockDBOpenHelper.Symbols._SYMBOL, symbol.getSymbol());
        values.put(StockDBOpenHelper.Symbols._NAME, symbol.getName());
        values.put(StockDBOpenHelper.Symbols._EXCHANGE, symbol.getExchange());

        db.insertWithOnConflict(StockDBOpenHelper.Symbols.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

        /*
    @Override
    public void deleteAllSymbols() {
        delete(Symbols.TABLE_NAME, String.format("%s!='%s'", Symbols._NAME, ".") );
    }
    */
}
