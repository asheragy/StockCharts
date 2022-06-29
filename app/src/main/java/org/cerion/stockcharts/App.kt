package org.cerion.stockcharts

import android.app.Application
import org.cerion.marketdata.core.charts.ChartColors
import org.cerion.marketdata.core.repository.CachedPriceListRepository
import org.cerion.marketdata.core.repository.PriceListRepository
import org.cerion.marketdata.core.web.PriceHistoryDataSource
import org.cerion.marketdata.core.web.clients.TDAmeritrade
import org.cerion.marketdata.webclients.yahoo.YahooFinance
import org.cerion.stockcharts.database.AppDatabase
import org.cerion.stockcharts.database.SymbolDao
import org.cerion.stockcharts.database.getDatabase
import org.cerion.stockcharts.repository.*
import org.cerion.stockcharts.ui.charts.ChartColorScheme
import org.cerion.stockcharts.ui.charts.ChartsViewModel
import org.cerion.stockcharts.ui.positions.PositionsViewModel
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
    single<AndroidPriceListRepository> { PriceListSQLRepository(get()) }  // Needed for AndroidPriceListRepository
    single<PriceListRepository> { PriceListSQLRepository(get()) }        // Needed for cachedRepository
    single { SymbolRepository(get() as SymbolDao) } // TODO remove cast after 2nd constructor is removed
    single<PreferenceRepository> { DefaultPreferenceRepository(get()) }
    single { CachedPriceListRepository(get(), get() ) }
}

val viewModelModule = module {
    single<ChartColors> { ChartColorScheme(get()) }
    viewModel { SymbolsViewModel(get(), get()) }
    viewModel { SymbolDetailsViewModel(get()) }
    viewModel { ChartsViewModel(get(), get(), get(), get()) }
    viewModel { PositionsViewModel(get(), get()) }
}