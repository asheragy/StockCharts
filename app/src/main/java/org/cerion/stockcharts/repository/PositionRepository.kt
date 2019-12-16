package org.cerion.stockcharts.repository

import org.cerion.stockcharts.BuildConfig
import org.cerion.stockcharts.database.Account
import org.cerion.stockcharts.database.AccountDao
import org.cerion.stocks.core.model.Position
import org.cerion.stocks.core.web.api.RequestException
import org.cerion.stocks.core.web.api.TDAmeritrade
import org.cerion.stocks.core.web.api.TDAmeritradeAuth


class GenericPosition(override val symbol: String, override val quantity: Double, override val pricePerShare: Double, override val cash: Boolean = false) : Position {
    override val totalValue: Double
        get() = quantity * pricePerShare
}

class PositionRepository(private val dao: AccountDao) {

    // TODO add caching

    fun getPositionsForAccount(account: Account): List<Position> {

        try {
            // TODO this should check for expired key and not rely on the exception
            val api = TDAmeritrade(account.authToken)
            return api.getPositions()
        }
        catch (e: RequestException) {
            val auth = TDAmeritradeAuth(BuildConfig.CONSUMER_KEY, BuildConfig.REDIRECT_URI)
            val response = auth.refreshAuth(account.refreshToken)

            account.authToken = response.accessToken
            dao.update(account)

            return TDAmeritrade(response.accessToken).getPositions()
        }

        return emptyList()

        /*
        Thread.sleep(500)
        when(account.id) {
            2 -> return listOf(
                    GenericPosition("SPY", 32.0, 314.87),
                    GenericPosition("Money Market", 700.56, 1.0, true))

            5 -> return listOf(
                    GenericPosition("XLE", 73.0, 67.92),
                    GenericPosition("XLE", 65.0, 76.30),
                    GenericPosition("OHI", 125.0, 40.5999))
        }

        return emptyList()

         */
    }


}