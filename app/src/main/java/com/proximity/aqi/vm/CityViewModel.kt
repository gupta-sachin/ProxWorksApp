package com.proximity.aqi.vm

import android.util.Log
import androidx.annotation.ColorRes
import androidx.lifecycle.*
import com.proximity.app.BuildConfig
import com.proximity.app.R
import com.proximity.aqi.data.AqiChartEntry
import com.proximity.aqi.data.City
import com.proximity.aqi.data.CityUi
import com.proximity.aqi.data.Event
import com.proximity.aqi.data.repository.CityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private const val LOG_TAG = "CityViewModel"

class CityViewModel(private val repository: CityRepository) : ViewModel() {

    fun connect() {
        repository.connect(this)
    }

    fun closeConnection() {
        repository.closeConnection()
    }

    fun insertOrUpdate(cities: List<City>) = viewModelScope.launch {
        repository.insertOrUpdate(cities)
    }

    ///////////////////////////

    private val ONE_SECOND_AS_MILLIS = TimeUnit.SECONDS.toMillis(1)

    private val ONE_MINUTE_AS_MILLIS = TimeUnit.MINUTES.toMillis(1)

    private val SimpleDateFormat_MMM_dd_yyyy =
        SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).also {
            it.timeZone = TimeZone.getDefault()
        }

    private val SimpleDateFormat_hh_mm_a = SimpleDateFormat("hh:mm a", Locale.ENGLISH).also {
        it.timeZone = TimeZone.getDefault()
    }

    // Using LiveData and caching the shown data has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    @JvmField
    val citiesLiveData: LiveData<List<CityUi>> = repository.citiesFlow.map { list ->
        list.map {
            CityUi(
                it.city,
                String.format("%.2f", it.aqi),
                getDisplayTime(it.time),
                getAqiTextColor(it.aqi)
            )
        }
    }.asLiveData()

    private fun getDisplayTime(time: Long): String {
        val currentTime = System.currentTimeMillis()
        val gap = currentTime - time
        return when {
            gap < 15 * ONE_SECOND_AS_MILLIS -> "A few seconds ago"
            gap < ONE_MINUTE_AS_MILLIS -> "${gap / 1000} seconds ago"
            gap < 2 * ONE_MINUTE_AS_MILLIS -> "A minute ago"
            else -> {
                val dateOfGivenTime = Date(time)
                val formattedDateOfGivenTime = SimpleDateFormat_MMM_dd_yyyy.format(dateOfGivenTime)
                val formattedDateOfToday = SimpleDateFormat_MMM_dd_yyyy.format(Date())
                if (formattedDateOfGivenTime == formattedDateOfToday) {
                    SimpleDateFormat_hh_mm_a.format(dateOfGivenTime)
                } else {
                    return formattedDateOfGivenTime
                }
            }
        }
    }

    @ColorRes
    private fun getAqiTextColor(aqi: Double): Int {
        return when {
            aqi <= 50 -> R.color.aqi_0_50_good
            aqi <= 100 -> R.color.aqi_51_100_satisfactory
            aqi <= 200 -> R.color.aqi_101_200_moderate
            aqi <= 300 -> R.color.aqi_201_300_poor
            aqi <= 400 -> R.color.aqi_301_400_very_poor
            else -> R.color.aqi_401_500_severe
        }
    }

    /////////////////////////////

    private val _eventItemClickedLiveData = MutableLiveData<Event.ItemClicked>()

    @JvmField
    val eventItemClickedLiveData: LiveData<Event.ItemClicked> = _eventItemClickedLiveData

    fun sendEventItemClicked(city: String) {
        _eventItemClickedLiveData.value = Event.ItemClicked(city)
    }

    /////////////////////////////

    private val MIN_INTERVAL_MILLIS = 30 * ONE_SECOND_AS_MILLIS

    private var mFirstEntryMillis: Long = -1L
    private var mLastEntryMillis: Long = -1L

    private var getAQIsJob: Job? = null

    private val _aqisListLiveData = MutableLiveData<List<AqiChartEntry>>()

    @JvmField
    val aqisListLiveData: LiveData<List<AqiChartEntry>> = _aqisListLiveData

    fun getAQIs(city: String) {
        // Clear previous list here instead of inside coroutine.
        // It is needed to ensure that observer will not receive previous data as soon as this method is called.
        // And though it is must when different city is selected, we are clearing without checking the city,
        // and thus, we will always fetch city-wise data from database and filter by interval, even if any city is re-selected.
        // TODO - if we want to optimize it, we will need to have city-wise view-model-level cache.
        if (_aqisListLiveData.value != null) {
            _aqisListLiveData.value = emptyList()
        }

        getAQIsJob?.cancel() // cancel previous job, if still active
        getAQIsJob = viewModelScope.launch(Dispatchers.IO) {
            // clear previous data
            mFirstEntryMillis = -1
            mLastEntryMillis = -1

            // fetch/filter new data
            val list = repository.getAQIs(city)
            val filteredList = mutableListOf<AqiChartEntry>()
            for (entry in list) {
                if (isEntryToBeShown(entry)) {
                    filteredList.add(entry)
                }
            }
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "getAQIs() ${list.size} ${filteredList.size}")
            }
            if (isActive) {
                _aqisListLiveData.postValue(filteredList)
            }
        }
    }

    //////////////////////////

    /**
     * Ideally, [getAQIs] must already be called before calling this method, and in that case,
     * the first latest entry will always be dropped by [isEntryToBeShown]
     */
    fun getLatestAqiAsLiveData(city: String): LiveData<AqiChartEntry> =
        repository.getLatestAQIinFlow(city)
            .distinctUntilChanged() // filter duplicates (though isEntryToBeShown can filter duplicates too)
            .filter { isEntryToBeShown(it) } // filter by interval
            .asLiveData()

    /**
     * Checks if aqiChartEntry is to be shown on chart as per MIN_INTERVAL_MILLIS,
     * Also sets the value of [AqiChartEntry.secondsSinceFirstEntry]
     *
     * @return true if entry is to be shown on chart, false otherwise
     */
    // TODO - if city-wise view-model-level cache will be implemented, each new shown entry will need to be added to that list
    private fun isEntryToBeShown(aqiChartEntry: AqiChartEntry): Boolean {
        if (mFirstEntryMillis == -1L) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "isEntryToBeShown first $aqiChartEntry")
            }
            mFirstEntryMillis = aqiChartEntry.time
            mLastEntryMillis = aqiChartEntry.time
            // aqiChartEntry.secondsSinceFirstEntry = 0f // default is also 0f
            return true
        }
        if (aqiChartEntry.time >= mLastEntryMillis + MIN_INTERVAL_MILLIS) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "isEntryToBeShown shown $aqiChartEntry")
            }
            mLastEntryMillis = aqiChartEntry.time
            aqiChartEntry.secondsSinceFirstEntry =
                ((aqiChartEntry.time - mFirstEntryMillis) / 1000).toFloat()
            return true
        }
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "isEntryToBeShown dropped $aqiChartEntry")
        }
        return false
    }

    /////////////////////

    private val xAxisValueLabelMap = mutableMapOf<Float, String>()

    private val SimpleDateFormat_hh_mm_ss_a = SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH).also {
        it.timeZone = TimeZone.getDefault()
    }

    fun getTimeLabel(secondsSinceStart: Float): String {
        val cachedLabel = xAxisValueLabelMap[secondsSinceStart]
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "getTimeLabel() $secondsSinceStart $cachedLabel")
        }
        if (cachedLabel != null) {
            return cachedLabel
        }
        val dateOfGivenTime = Date(mFirstEntryMillis + secondsSinceStart.toLong() * 1000)
        val formatted = SimpleDateFormat_hh_mm_ss_a.format(dateOfGivenTime)
        xAxisValueLabelMap[secondsSinceStart] = formatted
        return formatted
    }
}

class CityViewModelFactory(private val repository: CityRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}