package com.proximity.app

import android.app.Application
import com.proximity.aqi.data.repository.CityRepository
import com.proximity.aqi.data.source.local.CityRoomDatabase

class ProxWorksApp : Application() {
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    private val database by lazy { CityRoomDatabase.getDatabase(this) }
    val repository by lazy { CityRepository(database.wordDao()) }
}