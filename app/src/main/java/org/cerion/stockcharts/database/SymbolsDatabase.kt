package org.cerion.stockcharts.database

import android.content.Context
import android.preference.PreferenceManager
import androidx.room.*
import java.io.File
import java.io.FileOutputStream

@Database(entities = [SymbolEntity::class], version = 1)
abstract class SymbolsDatabase : RoomDatabase() {
    abstract val symbolsDao: SymbolLookupDao
}

@Dao
interface SymbolLookupDao {
    @Query("SELECT * FROM symbols WHERE symbol LIKE :partialSymbol")
    fun find(partialSymbol: String): List<SymbolEntity>

    @Query("SELECT * FROM symbols WHERE symbol LIKE :partialSymbol OR name LIKE :partialName")
    fun find(partialSymbol: String, partialName: String): List<SymbolEntity>
}

private lateinit var INSTANCE: SymbolsDatabase
fun getSymbolsDatabase(context: Context): SymbolsDatabase {
    synchronized(AppDatabase::class.java) {

        // First check if database has been extracted or used in assets
        verifyOrCreateDatabase(context)

        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext, SymbolsDatabase::class.java, "symbols.db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
        }

        return INSTANCE
    }
}

private val KEY_DATABASE_VERSION = "symbolsDatabaseVersion"
private val SymbolsDatabaseVersion = 1

private fun verifyOrCreateDatabase(context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val outFileName = context.applicationInfo.dataDir + "/databases/symbols.db"

    val existingVersion = prefs.getInt(KEY_DATABASE_VERSION, 0)
    if (existingVersion < SymbolsDatabaseVersion || !File(outFileName).exists()) {
        println("Creating or updating database from version $existingVersion to $SymbolsDatabaseVersion")

        // Create or overwrite database from assets folder
        val input= context.assets.open("symbols.db")
        val outFileName = context.applicationInfo.dataDir + "/databases/symbols.db"
        val output = FileOutputStream(outFileName)
        val buffer = ByteArray(4096)

        var length = 0
        while (input.read(buffer).also { length = it } > 0) {
            output.write(buffer, 0, length)
        }
        output.flush()
        output.close()
        input.close()

        prefs.edit().putInt(KEY_DATABASE_VERSION, SymbolsDatabaseVersion).apply()
    }
}