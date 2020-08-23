package org.cerion.stockcharts.fakes

import org.cerion.stockcharts.repository.AndroidPriceListRepository

class FakeAndroidPriceListRepository : AndroidPriceListRepository, FakePriceListRepository() {
    override suspend fun clearCache() {
    }

    override suspend fun cleanupCache() {
    }
}