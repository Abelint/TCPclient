package com.lekciya.tcpclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.net.Socket

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val connectButton = findViewById<Button>(R.id.connectButton)
        val responseTextView = findViewById<TextView>(R.id.responseTextView)
        // Нажатие кнопки для подключения к серверу
        connectButton.setOnClickListener {
            val serverIP = "192.168.0.153"  // IP-адрес сервера
            val serverPort = 80  // Порт сервера
            val request = "/?id=4&name=b&status=true"  // Строка запроса

            // Запуск подключения в отдельном потоке
            connectToServer(serverIP, serverPort, request, object : TCPClientCallback {
                override fun onResponse(response: String) {
                    // Обновление UI с ответом от сервера (выполняется в основном потоке)
                    runOnUiThread {
                        responseTextView.text = response
                    }
                }

                override fun onError(error: String) {
                    // Обработка ошибок и обновление UI
                    runOnUiThread {
                        responseTextView.text = error
                    }
                }
            })
        }
    }

    // Функция для подключения к серверу с использованием callback
    private fun connectToServer(serverIP: String, serverPort: Int, request: String, callback: TCPClientCallback) {
        Thread {
            var socket: Socket? = null
            try {
                // Подключение к серверу
                socket = Socket(serverIP, serverPort)

                // Отправка запроса на сервер
                val outputStream: OutputStream = socket.getOutputStream()
                val printWriter = PrintWriter(outputStream, true)
                val httpRequest = "GET $request HTTP/1.1\r\nHost: $serverIP\r\n\r\n"
                printWriter.write(httpRequest)
                printWriter.flush()

                // Чтение ответа от сервера
                val inputStream = socket.getInputStream()
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                val stringBuilder = StringBuilder()

                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line).append("\n")
                }

                // Передача ответа через callback
                callback.onResponse(stringBuilder.toString())

            } catch (e: Exception) {
                // Передача ошибки через callback
                callback.onError("Ошибка подключения: ${e.message}")
            } finally {
                try {
                    socket?.close()
                } catch (e: Exception) {
                    Log.e("TCP", "Ошибка при закрытии сокета: ${e.message}")
                }
            }
        }.start() // Запуск в отдельном потоке
    }
}