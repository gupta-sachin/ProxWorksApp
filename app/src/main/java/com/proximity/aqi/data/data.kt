package com.proximity.aqi.data

import androidx.annotation.ColorRes
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "city_table")
data class City(
    val city: String,
    val aqi: Double,
    val time: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

data class CityUi(
    val city: String,
    val aqi: String,
    val time: String,
    @ColorRes val aqiTextColor: Int
)

data class AqiChartEntry(
    val aqi: Double,
    val time: Long
) {
    @Ignore
    var secondsSinceFirstEntry: Float = 0f
}

sealed class Event {
    data class ItemClicked(
        val city: String,
        var handled: Boolean = false
    ) : Event()
}