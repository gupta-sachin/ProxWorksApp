package com.proximity.aqi.data.source.remote

import android.os.Looper
import android.util.Log
import com.proximity.app.BuildConfig
import com.proximity.aqi.data.City
import com.proximity.aqi.vm.CityViewModel
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import java.net.URI
import javax.net.ssl.SSLSocketFactory

object CityAqiSource {

    private const val LOG_TAG = "CityAqiSource"

    private const val WEB_SOCKET_URL = "wss://city-ws.herokuapp.com/"

    private lateinit var webSocketClient: WebSocketClient

    private lateinit var viewModel: CityViewModel

    fun connect(viewModel: CityViewModel) {
        this.viewModel = viewModel
        createWebSocketClient()
        val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        webSocketClient.setSocketFactory(socketFactory)
        webSocketClient.connect()
    }

    fun closeConnection() {
        webSocketClient.close()
    }

    private fun createWebSocketClient() {
        webSocketClient = object : WebSocketClient(URI(WEB_SOCKET_URL)) {

            override fun onOpen(shs: ServerHandshake?) {
                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "onOpen ${shs?.httpStatus} ${shs?.httpStatusMessage}")
                }
            }

            override fun onMessage(message: String?) {
                if (BuildConfig.DEBUG) {
                    assert(Looper.getMainLooper() != Looper.myLooper())
                    Log.d(LOG_TAG, "onMessage: $message")
                }
                if (message.isNullOrBlank()) {
                    return
                }
                try {
                    val jsonArray = JSONArray(message)
                    val time = System.currentTimeMillis()
                    val cities = mutableListOf<City>()
                    for (i in 0 until jsonArray.length()) {
                        try {
                            val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                            val city = jsonObject.getString("city")
                            val aqi = jsonObject.getDouble("aqi")
                            cities.add(City(city, aqi, time))
                        } catch (e: JSONException) {
                            if (BuildConfig.DEBUG) {
                                Log.e(LOG_TAG, "onMessage i:$i", e)
                            }
                        }
                    }
                    viewModel.insertOrUpdate(cities)
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Log.e(LOG_TAG, "onMessage", e)
                    }
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "onClose $code $reason $remote")
                }
            }

            override fun onError(ex: Exception?) {
                if (BuildConfig.DEBUG) {
                    Log.e(LOG_TAG, "onError", ex)
                }
            }
        }
    }
}