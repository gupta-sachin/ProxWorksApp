package com.proximity.aqi.data.source.remote

import android.os.Looper
import android.util.Log
import com.proximity.app.BuildConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import javax.net.ssl.SSLSocketFactory

object CityAqiSource {

    private const val LOG_TAG = "CityAqiSource"

    private const val WEB_SOCKET_URL = "wss://city-ws.herokuapp.com/"

    private lateinit var webSocketClient: WebSocketClient

    private var isConnected: Boolean = false

    private var message: String? = null

    fun connect(): Flow<String> = flow {
        closeConnection()
        createWebSocketClient()
        val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        webSocketClient.setSocketFactory(socketFactory)
        webSocketClient.connect()
        isConnected = true
        while (isConnected) {
            if (BuildConfig.DEBUG) {
                val isBgThread = Looper.getMainLooper() != Looper.myLooper()
                Log.v(LOG_TAG, "while $isConnected $isBgThread $message")
            }
            val _message = message
            if (_message != null) {
                try {
                    emit(_message)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                message = null
            }
            delay(1000)
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

            override fun onMessage(_message: String?) {
                if (BuildConfig.DEBUG) {
                    Log.v(LOG_TAG, "onMessage: $_message")
                }
                if (_message.isNullOrBlank()) {
                    return
                }
                message = _message
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