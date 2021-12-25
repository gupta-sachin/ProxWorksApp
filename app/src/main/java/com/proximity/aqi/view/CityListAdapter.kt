package com.proximity.aqi.view

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proximity.app.BuildConfig
import com.proximity.app.R
import com.proximity.aqi.data.CityUi
import com.proximity.aqi.vm.CityViewModel

private val PAYLOAD_AQI = Any()
val PAYLOAD_COLOR = Any()
private val PAYLOAD_TIME = Any()
private val PAYLOAD_AQI_COLOR = Any()
private val PAYLOAD_AQI_TIME = Any()
private val PAYLOAD_COLOR_TIME = Any()
private val PAYLOAD_AQI_COLOR_TIME = Any()

private val COMPARATOR = object : DiffUtil.ItemCallback<CityUi>() {

    override fun areContentsTheSame(oldItem: CityUi, newItem: CityUi): Boolean {
        if (BuildConfig.DEBUG) {
            Log.d("DiffUtil", "content ${oldItem.city} ${oldItem == newItem}")
        }
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: CityUi, newItem: CityUi): Boolean {
        if (BuildConfig.DEBUG) {
            Log.d("DiffUtil", "item ${oldItem.city} ${newItem.city}")
        }
        return oldItem.city == newItem.city
    }

    override fun getChangePayload(oldItem: CityUi, newItem: CityUi): Any? {
        if (BuildConfig.DEBUG) {
            Log.d("DiffUtil", "getChangePayload() $oldItem $newItem")
        }
        return if (oldItem.aqiTextColor != newItem.aqiTextColor) {
            PAYLOAD_COLOR
        } else {
            PAYLOAD_AQI_TIME
        }
//        val aqiChanged = oldItem.aqi != newItem.aqi
//        val aqiColorBandChanged = oldItem.aqiTextColor != newItem.aqiTextColor
//        val displayTimeChanged = oldItem.time != newItem.time
//        return when {
//            aqiChanged && aqiColorBandChanged && displayTimeChanged -> PAYLOAD_AQI_COLOR_TIME
//            aqiChanged && aqiColorBandChanged -> PAYLOAD_AQI_COLOR
//            aqiChanged && displayTimeChanged -> PAYLOAD_AQI_TIME
//            aqiColorBandChanged && displayTimeChanged -> PAYLOAD_COLOR_TIME
//            aqiChanged -> PAYLOAD_AQI
//            aqiColorBandChanged -> PAYLOAD_COLOR
//            displayTimeChanged -> PAYLOAD_TIME
//            else -> null
//        }
    }
}

private const val LOG_TAG = "CityListAdapter"

class CityListAdapter(private val viewModel: CityViewModel) :
    ListAdapter<CityUi, CityViewHolder>(COMPARATOR) {

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(position, it)
        }
    }

    override fun onBindViewHolder(
        holder: CityViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        getItem(position)?.let {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "onBind ${it.city} $payloads")
            }
            if (payloads.isEmpty()) {
                holder.bind(position, it)
            } else {
                holder.update(it, payloads)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        return CityViewHolder.create(parent)
    }

    internal fun onItemClick(position: Int) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "onItemClick $position")
        }
        if (position == RecyclerView.NO_POSITION) {
            return // ideally it should never be so by user action
        }
        getItem(position)?.let {
            viewModel.sendEventItemClicked(position, it)
        }
    }
}