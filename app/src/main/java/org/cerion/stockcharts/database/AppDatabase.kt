package org.cerion.stockcharts.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [Account::class, PriceListEntity::class, PriceRowEntity::class, SymbolEntity::class], version = 5)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val accountDao: AccountDao
    abstract val priceListDao: PriceListDao
    abstract val pricesDao: PricesDao
    abstract val symbolsDao: SymbolDao
}

private lateinit var INSTANCE: AppDatabase
fun getDatabase(context: Context): AppDatabase {
    synchronized(AppDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app.db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
        }

        return INSTANCE
    }
}
