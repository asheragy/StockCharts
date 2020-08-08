package org.cerion.stockcharts.ui.charts

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runBlockingTest
import org.cerion.stockcharts.R
import org.cerion.stockcharts.repository.PreferenceRepository
import org.cerion.stockcharts.ui.charts.fakes.FakePreferenceRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest


val testModules = module {
    single<PreferenceRepository>(override = true) { FakePreferenceRepository() }
}

@RunWith(AndroidJUnit4::class)
//@ExperimentalCoroutinesApi
class ChartsFragmentTest : KoinTest {

    @Before
    fun before() {
        loadKoinModules(listOf(testModules))
    }

    @Test
    fun chartsFragment_basicRun() = runBlockingTest {
        launchFragmentInContainer<ChartsFragment>(null, R.style.AppTheme)

        Thread.sleep(5000)
    }
}