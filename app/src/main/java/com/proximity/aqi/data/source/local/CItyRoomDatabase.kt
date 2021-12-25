package com.proximity.aqi.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.proximity.aqi.data.City

// Annotates class to be a Room Database with a table (entity) of the City class
@Database(entities = [City::class], version = 1, exportSchema = false)
abstract class CityRoomDatabase : RoomDatabase() {

    abstract fun wordDao(): CityDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: CityRoomDatabase? = null

        fun getDatabase(context: Context): CityRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        CityRoomDatabase::class.java,
                        "city_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}