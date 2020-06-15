package org.cerion.stockcharts

import android.app.Application
import org.cerion.stockcharts.database.AppDatabase
import org.cerion.stockcharts.database.SymbolDao
import org.cerion.stockcharts.database.getDatabase
import org.cerion.stockcharts.repository.PriceListSQLRepository
import org.cerion.stockcharts.repository.SymbolRepository
import org.cerion.stockcharts.ui.symbols.SymbolDetailsViewModel
import org.cerion.stockcharts.ui.symbols.SymbolsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)

            modules(listOf(
                    databaseModule,
                    repositoryModule,
                    viewModelModule
            ))
        }
    }
}

val databaseModule = module {
    single { getDatabase(get()) }
    single { (get() as AppDatabase).symbolsDao }
}

val repositoryModule = module {
    single { PriceListSQLRepository(get()) }
    single { SymbolRepository(get() as SymbolDao) } // TODO remove cast after 2nd constructor is removed
}

val viewModelModule = module {
    viewModel { SymbolsViewModel(get(), get()) }
    viewModel { SymbolDetailsViewModel(get()) }
}