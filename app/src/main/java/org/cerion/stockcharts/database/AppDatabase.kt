package org.cerion.stockcharts.database

import android.content.Context
import androidx.room.*


@Database(entities = [Account::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract val accountDao: AccountDao
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
