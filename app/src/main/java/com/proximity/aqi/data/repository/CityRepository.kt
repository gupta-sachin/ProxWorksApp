package com.proximity.aqi.data.repository

import android.os.Looper
import android.util.Log
import com.proximity.app.BuildConfig
import com.proximity.aqi.data.AqiChartEntry
import com.proximity.aqi.data.City
import com.proximity.aqi.data.source.local.CityDao
import com.proximity.aqi.data.source.remote.CityAqiSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception

private const val LOG_TAG = "CityRepository"

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class CityRepository(private val cityDao: CityDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    @JvmField
    val citiesFlow: Flow<List<City>> = cityDao.getCities()

    suspend fun connect() {
        CityAqiSource.connect().map { message ->
            if (BuildConfig.DEBUG) {
                val isBgThread = Looper.getMainLooper() != Looper.myLooper()
                Log.v(LOG_TAG, "map $isBgThread $message")
            }
            val cities = mutableListOf<City>()
            try {
                val jsonArray = JSONArray(message)
                val time = System.currentTimeMillis()

                for (i in 0 until jsonArray.length()) {
                    try {
                        val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                        val city = jsonObject.getString("city")
                        val aqi = jsonObject.getDouble("aqi")
                        cities.add(City(city, aqi, time))
                    } catch (e: JSONException) {
                        if (BuildConfig.DEBUG) {
                            Log.e(LOG_TAG, "onMessage i:$i", e)
                        }
                    }
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e(LOG_TAG, "onMessage", e)
                }
            }
            cities
        }.collectLatest {
            if (it.isNotEmpty()) {
                cityDao.insert(it)
            }
        }
    }

    fun closeConnection() {
        CityAqiSource.closeConnection()
    }

    suspend fun getAQIs(city: String): List<AqiChartEntry> = cityDao.getAQIs(city)

    fun getLatestAQIinFlow(city: String): Flow<AqiChartEntry> = cityDao.getLatestAQIinFlow(city)
}