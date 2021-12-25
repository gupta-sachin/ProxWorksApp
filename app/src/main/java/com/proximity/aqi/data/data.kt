package com.proximity.aqi.data

import androidx.annotation.ColorRes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_table")
data class City(@PrimaryKey val city: String, val aqi: Double, val time: Long)

data class CityUi(
    val city: String,
    val aqi: String,
    val time: String,
    @ColorRes val aqiTextColor: Int
)