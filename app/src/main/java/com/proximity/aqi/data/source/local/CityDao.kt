package com.proximity.aqi.data.source.local

import android.util.Log
import androidx.room.*
import com.proximity.app.BuildConfig
import com.proximity.aqi.data.City
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Query("SELECT * FROM city_table ORDER BY city ASC")
    fun getCities(): Flow<List<City>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cities: List<City>): List<Long>

    @Update
    suspend fun update(cities: List<City>)

    @Transaction
    suspend fun insertOrUpdate(cities: List<City>) {
        val insertResults = insert(cities)
        val toBeUpdated = mutableListOf<City>()

        for (i in insertResults.indices) {
            if (insertResults[i] == -1L) {
                toBeUpdated.add(cities[i])
            }
        }
        if (BuildConfig.DEBUG) {
            Log.d("CityDao", "all:${cities.size} updated:${toBeUpdated.size}")
        }

        if (toBeUpdated.isNotEmpty()) {
            update(toBeUpdated)
        }
    }
}