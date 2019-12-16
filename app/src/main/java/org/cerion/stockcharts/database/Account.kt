package org.cerion.stockcharts.database

import androidx.room.*

@Dao
interface AccountDao {

    @Query("select * from accounts")
    fun getAll(): List<Account>

    @Insert
    fun insert(account: Account)

    @Update
    fun update(account: Account)
}

@Entity(tableName = "accounts")
data class Account(
        val name: String,
        val refreshToken: String,
        var authToken: String) {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    override fun toString(): String = name
}