package com.proximity.aqi.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.proximity.app.R

class AqiMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aqi_main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, CitiesListFragment.newInstance())
                .commitNow()
        }
    }
}