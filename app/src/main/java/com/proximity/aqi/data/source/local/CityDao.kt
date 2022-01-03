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

    // OnConflictStrategy is not needed as we have a @PrimaryKey with (autoGenerate = true)
    @Insert
    suspend fun insert(cities: List<City>)

    // return all rows for query city
    @Query("SELECT aqi,time FROM city_table WHERE city = :query ORDER BY time ASC")
    suspend fun getAQIs(query: String): List<AqiChartEntry>

    // return latest row for query city
    @Query("SELECT aqi,time FROM city_table WHERE city = :query ORDER BY time DESC LIMIT 1")
    fun getLatestAQIinFlow(query: String): Flow<AqiChartEntry>
}