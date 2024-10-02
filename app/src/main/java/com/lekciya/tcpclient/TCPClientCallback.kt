package com.lekciya.tcpclient

interface TCPClientCallback {
    fun onResponse(response: String)
    fun onError(error: String)
}