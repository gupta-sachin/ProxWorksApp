package com.proximity.aqi.data.repository

import com.proximity.aqi.data.AqiChartEntry
import com.proximity.aqi.data.City
import com.proximity.aqi.data.source.local.CityDao
import com.proximity.aqi.data.source.remote.CityAqiSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class CityRepository(private val cityDao: CityDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    @JvmField
    val citiesFlow: Flow<List<City>> = cityDao.getCities()

    suspend fun connect() {
        CityAqiSource.connect()
            .catch { e ->
                e.printStackTrace() // TODO - show error on UI, if needed
            }
            .collectLatest {
                cityDao.insert(it)
            }
    }

    fun closeConnection() {
        CityAqiSource.closeConnection()
    }

    suspend fun getAQIs(city: String): List<AqiChartEntry> = cityDao.getAQIs(city)

    fun getLatestAQIinFlow(city: String): Flow<AqiChartEntry> = cityDao.getLatestAQIinFlow(city)
}