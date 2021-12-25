package com.proximity.aqi.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.proximity.app.BuildConfig
import com.proximity.app.ProxWorksApp
import com.proximity.app.R
import com.proximity.aqi.vm.CityViewModel
import com.proximity.aqi.vm.CityViewModelFactory

private const val LOG_TAG = "AqiMainActivity"

class AqiMainActivity : AppCompatActivity() {

    private val viewModel: CityViewModel by viewModels {
        CityViewModelFactory((application as ProxWorksApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aqi_main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add<CitiesListFragment>(R.id.container)
            }
        }

        viewModel.eventItemClickedLiveData.observe(this) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "ItemClickedLiveData ${it.city} ${it.handled}")
            }
            if (it.handled) {
                return@observe
            }
            it.handled = true
            supportFragmentManager.commit {
                addToBackStack(null)
                replace<CityAqiChartFragment>(R.id.container)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.connect()
    }

    override fun onPause() {
        viewModel.closeConnection()
        super.onPause()
    }
}