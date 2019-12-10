package org.cerion.stockcharts.repository

import org.cerion.stocks.core.model.Position


class GenericPosition(override val symbol: String, override val quantity: Double, override val pricePerShare: Double, override val cash: Boolean = false) : Position {
    override val totalValue: Double
        get() = quantity * pricePerShare
}

class PositionRepository {

    fun getPositionsForAccount(account: Account): List<Position> {

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
    }


}