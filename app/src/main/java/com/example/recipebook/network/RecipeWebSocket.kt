package com.example.recipebook.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class RecipeWebSocket(
    private val onUpdate: () -> Unit
) {

    private val client = OkHttpClient()
    private lateinit var socket: WebSocket

    fun connect() {
        val request = Request.Builder()
            .url("ws://10.0.2.2:3000")
            .build()

        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Message received: $text")
                onUpdate()
            }
        })
    }
}
