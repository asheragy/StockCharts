package org.cerion.stockcharts.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cerion.stockcharts.common.TAG
import org.cerion.stockcharts.database.PriceListEntity
import org.cerion.stockcharts.database.PriceRowEntity
import org.cerion.stockcharts.database.getDatabase
import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.PriceRow
import org.cerion.stocks.core.model.FetchInterval
import org.cerion.stocks.core.platform.KMPDate
import org.cerion.stocks.core.repository.IPriceListRepository
import java.util.*

class PriceListSQLRepository(private val context: Context) : IPriceListRepository {

    private val roomDb = getDatabase(context)
    private val priceListDao = roomDb.priceListDao
    private val pricesDao = roomDb.pricesDao

    override fun get(symbol: String, interval: FetchInterval): PriceList? {
        val header = priceListDao.get(symbol, interval.ordinal)
                ?: return null

        val dbPrices = pricesDao.getAll(symbol, interval.ordinal)

        val prices = dbPrices.map {
            PriceRow(KMPDate(it.date), it.open, it.high, it.low, it.close, it.volume)
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
                PriceRowEntity(list.symbol, interval, it.date.jvmDate, it.open, it.high, it.low, it.close, it.volume)
            }

            pricesDao.insert(dbPrices)
        }
    }

    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            val name = roomDb.openHelper.databaseName
            val file = context.getDatabasePath(name)

            Log.i("Main", "Size before compact: ${file.length()}")
            priceListDao.deleteAll()
            pricesDao.deleteAll()
            roomDb.compact() // TODO how to verify this worked and how much space was saved

            // There is a delay for when this gets reported correctly, ignore for now
            //Log.i("Main", "Size after compact: ${file.length()}")
        }
    }

    suspend fun cleanupCache() {
        withContext(Dispatchers.IO) {
            val name = roomDb.openHelper.databaseName
            val file = context.getDatabasePath(name)
            Log.i("Main", "Size before compact: ${file.length()}")

            val lists = priceListDao.getAll().sortedBy { it.lastUpdated }
            val maxToKeep = 20
            val minToKeep = 5
            if (lists.size > maxToKeep) {
                val delete = lists.subList(0, lists.size - minToKeep)
                Log.i(TAG, "Deleting ${delete.size} cached lists")
                delete.forEach {
                    pricesDao.delete(it.symbol, it.interval)
                    priceListDao.delete(it)
                }

                roomDb.compact()
            }
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