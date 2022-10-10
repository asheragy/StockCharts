package org.cerion.stockcharts.repository

import org.cerion.marketdata.core.model.Interval
import org.cerion.marketdata.core.model.OHLCVTable
import org.cerion.marketdata.core.platform.KMPDate
import org.cerion.marketdata.webclients.FetchInterval
import org.cerion.marketdata.webclients.PriceHistoryDataSource
import java.time.LocalDate
import java.util.*

interface PriceHistoryDates {
    val dailyStartDate: KMPDate?
    val weeklyStartDate: KMPDate?
    val monthlyStartDate: KMPDate?
    val quarterStartDate: KMPDate?
}

class DefaultPriceHistoryDates : PriceHistoryDates {
    override val dailyStartDate = getYearsBack(5)
    override val weeklyStartDate = getYearsBack(10)
    override val monthlyStartDate = getYearsBack(20)
    override val quarterStartDate: KMPDate? = null

    companion object {
        fun getYearsBack(years: Int): KMPDate {
            return KMPDate(LocalDate.now().minusYears(years.toLong()))
        }
    }
}

class CachedPriceListRepository(private val repo: PriceListRepository, private val api: PriceHistoryDataSource, private val dates: PriceHistoryDates = DefaultPriceHistoryDates()) {

    fun get(symbol: String, interval: Interval): OHLCVTable {
        val fetchInterval = when(interval) {
            Interval.DAILY -> FetchInterval.DAILY
            Interval.WEEKLY -> FetchInterval.WEEKLY
            else -> FetchInterval.MONTHLY
        }

        val cachedResult = repo.get(symbol, fetchInterval)
        var update = false
        val retrieveFrom: Date? = null

        if (cachedResult.first == null) {
            update = true
        }
        else if(cachedResult.second != null) {
            val lastUpdated = cachedResult.second
            val now = Date()
            var diff = now.time - lastUpdated!!.time
            diff /= (1000 * 60 * 60).toLong()
            val hours = diff
            val days = diff / 24

            println(symbol + " " + fetchInterval.name + " last updated " + lastUpdated + " (" + days + " days ago)")

            // TODO, smarter updates based on last price obtained and weekends
            if (fetchInterval === FetchInterval.DAILY && hours >= 12)
                update = true
            else if (fetchInterval === FetchInterval.WEEKLY && days > 3)
                update = true
            else if (fetchInterval === FetchInterval.MONTHLY && days > 7)
                update = true

            // Incremental update, not sure if all this is necessary but start a few data points earlier to be safe
            // TODO this may be working but do full update and re-verify this later
            /*
            if (update) {
                val cal = Calendar.getInstance()
                cal.time = result.last.date

                when (interval) {
                    Interval.DAILY -> cal.add(Calendar.DAY_OF_MONTH, -1)
                    Interval.WEEKLY -> cal.add(Calendar.DAY_OF_MONTH, -7)
                    Interval.MONTHLY -> cal.add(Calendar.DAY_OF_MONTH, -31)
                    Interval.QUARTERLY,
                    Interval.YEARLY -> throw Exception("Only daily/weekly/monthly allowed")
                }

                retrieveFrom = cal.time
            }
             */
        }

        if (retrieveFrom != null) {
            throw NotImplementedError("add incremental updating")
            //updatePricesIncremental(symbol, interval, start, retrieveFrom)
        }

        val result = if (update)
            updatePrices(symbol, interval, fetchInterval)
        else
            cachedResult.first!!

        if (interval == Interval.MONTHLY && dates.monthlyStartDate != null) {
            if (dates.monthlyStartDate == null)
                return result

            // TODO this logic should go in the truncate function to not require exact
            var firstDate = result.dates.first()
            for(i in 0 until result.dates.size) {
                if (result.dates[i] >= dates.monthlyStartDate!!) {
                    firstDate = result.dates[i]
                    break
                }
            }

            return result.truncate(firstDate, null)
        }
        if (interval == Interval.QUARTERLY)
            return result.toQuarterly()
        if (interval == Interval.YEARLY)
            return result.toYearly()

        return result
    }

    private fun updatePrices(symbol: String, interval: Interval, fetchInterval: FetchInterval): OHLCVTable {
        var table: OHLCVTable? = null

        try {
            //val cal = Calendar.getInstance()
            //cal.set(1990, Calendar.JANUARY, 1)
            val kmpStartDate = when (interval) {
                Interval.DAILY -> dates.dailyStartDate
                Interval.WEEKLY -> dates.weeklyStartDate
                else -> dates.quarterStartDate // Always retrieve quarterly date then truncate month if needed
            }

            val startDate = kmpStartDate?.jvmDate

            val prices = api.getPrices(symbol, fetchInterval, startDate)
            table = OHLCVTable(symbol, prices)
        } catch (e: Exception) {
            // nothing
            e.printStackTrace()
        }

        if (table != null && table.size > 0) {
            repo.add(table)
            println("Updated prices for $symbol")
        } else {
            throw Exception("Failed to get updated prices for $symbol")
        }

        return table
    }
}