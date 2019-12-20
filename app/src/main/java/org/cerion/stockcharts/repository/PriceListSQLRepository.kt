package org.cerion.stockcharts.repository

import android.content.Context
import org.cerion.stockcharts.database.PriceListEntity
import org.cerion.stockcharts.database.PriceRowEntity
import org.cerion.stockcharts.database.StockDBOpenHelper
import org.cerion.stockcharts.database.getDatabase
import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.PriceRow
import org.cerion.stocks.core.model.HistoricalDates
import org.cerion.stocks.core.model.Interval
import org.cerion.stocks.core.repository.PriceListRepository
import java.util.*

class PriceListSQLRepository(context: Context) : SQLiteRepositoryBase(StockDBOpenHelper.getInstance(context)), PriceListRepository {

    private val roomDb = getDatabase(context)
    private val priceListDao = roomDb.priceListDao
    private val pricesDao = roomDb.pricesDao

    override fun get(symbol: String, interval: Interval, max: Int): PriceList {
        throw RuntimeException("not implemented")
    }

    override operator fun get(symbol: String, interval: Interval): List<PriceRow> {
        val dbValues = pricesDao.getAll(symbol, interval.ordinal)

        return dbValues.map {
            PriceRow(it.date, it.open, it.high, it.low, it.close, it.volume)
        }
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

    @Deprecated("Add last updated to pricelist and get list instead")
    override fun getHistoricalDates(symbol: String, interval: Interval): HistoricalDates? {
        val header = priceListDao.get(symbol, interval.ordinal)
        val prices = pricesDao.getAll(symbol, interval.ordinal)

        if (prices.isEmpty())
            return null

        return HistoricalDates().apply {
            this.symbol = symbol
            this.lastUpdated = header.lastUpdated
            this.firstDate = prices.minBy { it.date }?.date
            this.lastDate = prices.maxBy { it.date }?.date
        }
    }

    override fun deleteAll() {
        throw RuntimeException("not implemented")
        /*
        deleteAll(StockDBOpenHelper.Prices.getTableName(Interval.DAILY))
        deleteAll(StockDBOpenHelper.Prices.getTableName(Interval.WEEKLY))
        deleteAll(StockDBOpenHelper.Prices.getTableName(Interval.MONTHLY))
        deleteAll(StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_DAILY)
        deleteAll(StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_WEEKLY)
        deleteAll(StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_MONTHLY)
        optimize()
         */
    }
}