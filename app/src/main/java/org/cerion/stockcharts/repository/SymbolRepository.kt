package org.cerion.stockcharts.repository

import android.content.Context
import org.cerion.stockcharts.database.StockDBOpenHelper
import org.cerion.stockcharts.database.SymbolEntity
import org.cerion.stockcharts.database.getDatabase
import org.cerion.stocks.core.model.Symbol

class SymbolRepository(context: Context) : SQLiteRepositoryBase(StockDBOpenHelper.getInstance(context)) {

    private val symbolDao = getDatabase(context).symbolsDao

    fun getAll(): List<Symbol> {
        return symbolDao.getAll().map {
            Symbol(it.symbol, it.name, it.exchange)
        }
    }

    fun add(symbol: Symbol) {
        symbolDao.insert(
                SymbolEntity(symbol.symbol, symbol.name ?: "", symbol.exchange ?: ""))
    }

    fun delete(symbol: String) {
        symbolDao.delete(SymbolEntity(symbol, "", ""))
    }
}