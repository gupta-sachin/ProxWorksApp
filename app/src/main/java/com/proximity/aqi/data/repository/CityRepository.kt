package com.proximity.aqi.data.repository

import androidx.annotation.WorkerThread
import com.proximity.aqi.data.AqiChartEntry
import com.proximity.aqi.data.City
import com.proximity.aqi.data.source.local.CityDao
import com.proximity.aqi.data.source.remote.CityAqiSource
import com.proximity.aqi.vm.CityViewModel
import kotlinx.coroutines.flow.Flow

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class CityRepository(private val cityDao: CityDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val citiesFlow: Flow<List<City>> = cityDao.getCities()


    fun connect(viewModel: CityViewModel) {
        CityAqiSource.connect(viewModel)
    }

    fun closeConnection() {
        CityAqiSource.closeConnection()
    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertOrUpdate(cities: List<City>) {
        cityDao.insertOrUpdate(cities)
    }

    fun getCityAQIs(city: String): Flow<List<AqiChartEntry>> = cityDao.getCityAQIs(city)
}