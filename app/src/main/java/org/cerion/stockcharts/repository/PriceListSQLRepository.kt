package org.cerion.stockcharts.repository

import android.content.Context
import org.cerion.stockcharts.database.PriceListEntity
import org.cerion.stockcharts.database.PriceRowEntity
import org.cerion.stockcharts.database.StockDBOpenHelper
import org.cerion.stockcharts.database.getDatabase
import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.PriceRow
import org.cerion.stocks.core.model.Interval
import org.cerion.stocks.core.repository.IPriceListRepository
import java.util.*

class PriceListSQLRepository(context: Context) : SQLiteRepositoryBase(StockDBOpenHelper.getInstance(context)), IPriceListRepository {

    private val roomDb = getDatabase(context)
    private val priceListDao = roomDb.priceListDao
    private val pricesDao = roomDb.pricesDao

    override fun get(symbol: String, interval: Interval): PriceList? {
        val header = priceListDao.get(symbol, interval.ordinal)
                ?: return null

        val dbPrices = pricesDao.getAll(symbol, interval.ordinal)

        val prices = dbPrices.map {
            PriceRow(it.date, it.open, it.high, it.low, it.close, it.volume)
        }

        val result = PriceList(symbol, prices)
        result.lastUpdated = header.lastUpdated

        return result
    }

    override fun add(list: PriceList) {
        val interval = list.interval.ordinal

        // Delete existing + cascade deletes prices
        priceListDao.delete(list.symbol, interval)

        roomDb.runInTransaction {
            // Add list
            val priceList = PriceListEntity(list.symbol, interval)
            priceList.lastUpdated = Date()
            priceListDao.insert(priceList)

            // Add prices
            val dbPrices = list.map {
                PriceRowEntity(list.symbol, interval, it.date, it.open, it.high, it.low, it.close, it.volume)
            }

            pricesDao.insert(dbPrices)
        }
    }

    /*
    override fun deleteAll() {
        throw RuntimeException("not implemented")
        deleteAll(StockDBOpenHelper.Prices.getTableName(Interval.DAILY))
        deleteAll(StockDBOpenHelper.Prices.getTableName(Interval.WEEKLY))
        deleteAll(StockDBOpenHelper.Prices.getTableName(Interval.MONTHLY))
        deleteAll(StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_DAILY)
        deleteAll(StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_WEEKLY)
        deleteAll(StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_MONTHLY)
        optimize()
    }
     */
}