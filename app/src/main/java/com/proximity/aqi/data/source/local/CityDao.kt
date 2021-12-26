package com.proximity.aqi.data.source.local

import android.util.Log
import androidx.room.*
import com.proximity.app.BuildConfig
import com.proximity.aqi.data.AqiChartEntry
import com.proximity.aqi.data.City
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {

    // return latest row for each city
    @Query("SELECT * FROM (SELECT * FROM city_table ORDER BY city ASC, id ASC) GROUP BY city")
    fun getCities(): Flow<List<City>>

    // Below OnConflictStrategy is now useless, due to a new @PrimaryKey with (autoGenerate = true)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cities: List<City>): List<Long>

    // Below method is now useless for current features, due to a new @PrimaryKey with (autoGenerate = true)
    @Update
    suspend fun update(cities: List<City>)

    // Below method is now equivalent to [insert], due to a new @PrimaryKey with (autoGenerate = true)
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

    // return all rows for query city
    @Query("SELECT aqi,time FROM city_table WHERE city = :query ORDER BY time ASC")
    suspend fun getAQIs(query: String): List<AqiChartEntry>

    // return latest row for query city
    @Query("SELECT aqi,time FROM city_table WHERE city = :query ORDER BY time DESC LIMIT 1")
    fun getLatestAQIinFlow(query: String): Flow<AqiChartEntry>
}