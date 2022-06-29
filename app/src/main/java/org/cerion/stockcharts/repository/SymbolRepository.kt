package org.cerion.stockcharts.repository

import android.content.Context
import org.cerion.stockcharts.database.SymbolDao
import org.cerion.stockcharts.database.SymbolEntity
import org.cerion.stockcharts.database.getDatabase
import org.cerion.marketdata.core.model.Symbol

class SymbolRepository(private val dao: SymbolDao) {

    @Deprecated("use DAO constructor")
    constructor(context: Context) : this(getDatabase(context).symbolsDao)

    fun getAll(): List<Symbol> {
        return dao.getAll().map {
            Symbol(it.symbol, it.name, it.exchange)
        }
    }

    fun add(symbol: Symbol) {
        dao.insert(
                SymbolEntity(symbol.symbol, symbol.name ?: "", symbol.exchange ?: ""))
    }

    fun delete(symbol: String) {
        dao.delete(SymbolEntity(symbol, "", ""))
    }
}