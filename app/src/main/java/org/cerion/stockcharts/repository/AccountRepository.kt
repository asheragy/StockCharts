package org.cerion.stockcharts.repository


data class Account(val id: Int, val name: String) {
    override fun toString() = name
}

class AccountRepository {

    fun getAll(): List<Account> {
        return listOf(
                Account(2, "TD Ameritrade"),
                Account(5, "TD Fake Data"))
    }
}