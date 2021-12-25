package com.proximity.aqi.view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.proximity.app.BuildConfig
import com.proximity.app.R
import com.proximity.aqi.data.CityUi

class CityViewHolder private constructor(pItemView: View) : RecyclerView.ViewHolder(pItemView) {

    private val mTxvCity: TextView = pItemView.findViewById(R.id.txt_city)
    private val mTxvAqi: TextView = pItemView.findViewById(R.id.txt_aqi)
    private val mTxvTime: TextView = pItemView.findViewById(R.id.txt_time)

    init {
        itemView.setOnClickListener {

        }
    }

    fun bind(pPosition: Int, cityUi: CityUi) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "bind() $pPosition $cityUi")
        }
        mTxvCity.text = cityUi.city
        mTxvAqi.text = cityUi.aqi
        mTxvTime.text = cityUi.time
        mTxvAqi.setTextColor(itemView.resources.getColor(cityUi.aqiTextColor))
        mTxvAqi.setBackgroundColor(itemView.resources.getColor(R.color.gray_20))
    }

    fun update(cityUi: CityUi, payloads: MutableList<Any>) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "update() $cityUi")
        }
        mTxvAqi.text = cityUi.aqi
        mTxvTime.text = cityUi.time

        if (payloads.contains(PAYLOAD_COLOR)) {
            mTxvAqi.setTextColor(itemView.resources.getColor(cityUi.aqiTextColor))
            mTxvAqi.setBackgroundColor(itemView.resources.getColor(R.color.black))
        } else {
            mTxvAqi.setBackgroundColor(itemView.resources.getColor(R.color.gray_20))
        }
    }

    companion object {

        const val LOG_TAG = "CityViewHolder"

        fun create(pParent: ViewGroup): CityViewHolder {
            val inflater = LayoutInflater.from(pParent.context)
            return CityViewHolder(inflater.inflate(R.layout.item_city_aqi, pParent, false))
        }
    }
}
