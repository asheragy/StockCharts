package org.cerion.stockcharts.database

import androidx.room.*
import java.util.*

private const val PriceListTable = "pricelist"
private const val PricesTable = "prices"

@Dao
interface PriceListDao {
    @Query("select * from $PriceListTable")
    fun getAll(): List<PriceListEntity>

    @Query("select * from $PriceListTable where symbol=:symbol and interval=:interval")
    fun get(symbol: String, interval: Int): PriceListEntity?

    @Insert
    fun insert(priceList: PriceListEntity)

    // TODO should be able to replace with @Delete depending on usages
    @Query("delete from $PriceListTable where symbol=:symbol and interval=:interval")
    fun delete(symbol: String, interval: Int)

    @Delete
    fun delete(entity: PriceListEntity)

    @Query("delete from $PriceListTable")
    fun deleteAll()
}

@Dao
interface PricesDao {
    @Query("select * from $PricesTable where symbol=:symbol and interval=:interval")
    fun getAll(symbol: String, interval: Int): List<PriceRowEntity>

    @Insert
    fun insert(prices: List<PriceRowEntity>)

    @Query("delete from $PricesTable where symbol=:symbol and interval=:interval")
    fun delete(symbol: String, interval: Int)

    @Query("delete from $PricesTable")
    fun deleteAll()
}

@Entity(tableName = PriceListTable, primaryKeys = ["symbol", "interval"])
data class PriceListEntity(val symbol: String, val interval: Int) {
    var lastUpdated: Date? = null
}

@Entity(tableName = PricesTable,
        indices = [Index("symbol", "interval")],
        primaryKeys = ["symbol", "interval", "date"],
        foreignKeys = [ForeignKey(
                entity = PriceListEntity::class,
                parentColumns = arrayOf("symbol", "interval"),
                childColumns = arrayOf("symbol", "interval"),
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE)])
data class PriceRowEntity(
        val symbol: String,
        val interval: Int,
        val date: Date, // TODO should be localdate
        val open: Float,
        val high: Float,
        val low: Float,
        val close: Float,
        val volume: Float)