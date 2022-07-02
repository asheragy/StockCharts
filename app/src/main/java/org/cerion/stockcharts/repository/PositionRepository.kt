package org.cerion.stockcharts.repository

import org.cerion.stockcharts.database.Account
import org.cerion.stockcharts.database.AccountDao
import org.cerion.marketdata.core.model.Position
import org.cerion.marketdata.webclients.tda.RequestException
import org.cerion.marketdata.webclients.tda.TDAmeritrade
import java.util.*
import java.util.concurrent.TimeUnit


class PositionRepository(private val dao: AccountDao, private val tdapi: TDAmeritrade) {

    // TODO add caching

    fun getPositionsForAccount(account: Account): List<Position> {

        try {
            val diff = account.expires.time - Date().time
            val diffSeconds=  TimeUnit.MILLISECONDS.toSeconds(diff)

            // Refresh token if expired or close to expiring
            if (diffSeconds < 60) {
                val response = tdapi.refreshAuth(account.refreshToken)

                account.authToken = response.accessToken
                account.expires = response.expireDate
                dao.update(account)
            }

            return tdapi.getPositions(account.authToken)
        }
        catch (e: RequestException) {
            // TODO certain failure here means re-auth may be needed
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