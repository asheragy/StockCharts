package org.cerion.stockcharts

import android.app.Application
import org.cerion.stockcharts.database.AppDatabase
import org.cerion.stockcharts.database.SymbolDao
import org.cerion.stockcharts.database.getDatabase
import org.cerion.stockcharts.repository.PreferenceRepository
import org.cerion.stockcharts.repository.PriceListSQLRepository
import org.cerion.stockcharts.repository.SymbolRepository
import org.cerion.stockcharts.ui.charts.ChartColorScheme
import org.cerion.stockcharts.ui.charts.ChartsViewModel
import org.cerion.stockcharts.ui.positions.PositionsViewModel
import org.cerion.stockcharts.ui.symbols.SymbolDetailsViewModel
import org.cerion.stockcharts.ui.symbols.SymbolsViewModel
import org.cerion.stocks.core.repository.CachedPriceListRepository
import org.cerion.stocks.core.repository.IPriceListRepository
import org.cerion.stocks.core.web.PriceHistoryDataSource
import org.cerion.stocks.core.web.clients.TDAmeritrade
import org.cerion.stocks.core.web.clients.YahooFinance
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
                    networkModule,
                    databaseModule,
                    repositoryModule,
                    viewModelModule
            ))
        }
    }
}

val networkModule = module {
    single<PriceHistoryDataSource> { YahooFinance.instance }
    single { TDAmeritrade(BuildConfig.TD_CONSUMER_KEY, BuildConfig.TD_REDIRECT_URI) }
}

val databaseModule = module {
    single { getDatabase(get()) }
    single { (get() as AppDatabase).symbolsDao }
}

val repositoryModule = module {
    single { PriceListSQLRepository(get()) }
    single<IPriceListRepository> { PriceListSQLRepository(get()) }
    single { SymbolRepository(get() as SymbolDao) } // TODO remove cast after 2nd constructor is removed
    single { PreferenceRepository(get()) }
    single { CachedPriceListRepository(get(), get() ) }
}

val viewModelModule = module {
    single { ChartColorScheme(get()) }
    viewModel { SymbolsViewModel(get(), get()) }
    viewModel { SymbolDetailsViewModel(get()) }
    viewModel { ChartsViewModel(get(), get(), get(), get()) }
    viewModel { PositionsViewModel(get(), get()) }
}