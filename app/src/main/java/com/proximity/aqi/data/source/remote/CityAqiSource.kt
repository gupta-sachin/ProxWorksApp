package com.proximity.aqi.data.source.remote

import android.os.Looper
import android.util.Log
import com.proximity.app.BuildConfig
import com.proximity.aqi.data.City
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    @Volatile
    private var isConnected: Boolean = false

    private var citiesList: MutableList<City>? = null

    fun connect(): Flow<MutableList<City>> = flow {
        closeConnection()
        createWebSocketClient()
        val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        webSocketClient.setSocketFactory(socketFactory)
        webSocketClient.connect()
        isConnected = true
        while (isConnected) {
            val _citiesList = citiesList
            if (_citiesList != null && _citiesList.isNotEmpty()) {
                emit(_citiesList)
                citiesList = null
            }
            // TODO - can server add any extra field in message for appropriate/expected delay before next message
            delay(100)
        }
    }

    fun closeConnection() {
        if (::webSocketClient.isInitialized) {
            webSocketClient.close()
            isConnected = false
        }
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
                    val isBgThread = Looper.getMainLooper() != Looper.myLooper()
                    Log.v(LOG_TAG, "onMessage: $isBgThread $message")
                }
                if (message.isNullOrBlank()) {
                    return
                }
                val time = System.currentTimeMillis() // most accurate time can be fetched here
                val cities = mutableListOf<City>()
                try {
                    val jsonArray = JSONArray(message)

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
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Log.e(LOG_TAG, "onMessage", e)
                    }
                }
                citiesList = cities
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