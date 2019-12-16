package org.cerion.stockcharts.repository

import org.cerion.stockcharts.database.Account
import org.cerion.stockcharts.database.AccountDao




class AccountRepository(private val dao: AccountDao) {

    fun getAll(): List<Account> {

        return dao.getAll()
        /*
        return listOf(
                Account("TD Ameritrade", "", ""),
                Account("Fake Data", "" , ""))

         */
    }
}