package org.cerion.stockcharts.database

import androidx.room.*
import java.util.*

@Dao
interface PriceListDao {
    @Query("select * from pricelist where symbol=:symbol and interval=:interval")
    fun get(symbol: String, interval: Int): PriceListEntity

    @Insert
    fun insert(priceList: PriceListEntity)

    // TODO should be able to replace with @Delete depending on usages
    @Query("delete from pricelist where symbol=:symbol and interval=:interval")
    fun delete(symbol: String, interval: Int)
}

@Dao
interface PricesDao {
    @Query("select * from prices where symbol=:symbol and interval=:interval")
    fun getAll(symbol: String, interval: Int): List<PriceRowEntity>

    @Insert
    fun insert(prices: List<PriceRowEntity>)
}

@Entity(tableName = "pricelist", primaryKeys = ["symbol", "interval"])
data class PriceListEntity(val symbol: String, val interval: Int) {
    var lastUpdated: Date? = null
}

@Entity(tableName = "prices",
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
        val date: Date,
        val open: Float,
        val high: Float,
        val low: Float,
        val close: Float,
        val volume: Float)