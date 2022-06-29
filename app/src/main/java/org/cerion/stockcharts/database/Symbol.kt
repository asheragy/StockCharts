package org.cerion.stockcharts.database

import androidx.room.*
import org.cerion.marketdata.core.model.Symbol


@Entity(tableName = "symbols")
data class SymbolEntity(
        @PrimaryKey
        val symbol: String,
        val name: String,
        val exchange: String)

@Dao
interface SymbolDao {
    @Query("select * from symbols")
    fun getAll(): List<SymbolEntity>

    @Insert
    fun insert(symbol: SymbolEntity)

    @Delete
    fun delete(symbol: SymbolEntity)

}

fun SymbolEntity.toSymbol(): Symbol = Symbol(symbol, name, exchange)