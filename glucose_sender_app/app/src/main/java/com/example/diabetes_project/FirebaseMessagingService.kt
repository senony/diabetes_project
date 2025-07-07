package com.example.diabetes_project

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import androidx.core.app.NotificationCompat

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        // 서버로 토큰을 POST 방식으로 전송
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val url = "http://10.0.2.2:3000/register"  // 에뮬레이터의 localhost는 10.0.2.2

        val json = """
        {
            "token": "$token"
        }
    """.trimIndent()

        Thread {
            try {
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.doOutput = true

                connection.outputStream.use { os ->
                    val input = json.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                Log.d("FCM", "서버 응답 코드: $responseCode")
            } catch (e: Exception) {
                Log.e("FCM", "토큰 전송 실패", e)
            }
        }.start()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        Log.d("FCM", "Title: $title, Body: $body")
        showNotification(title, body)
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = "default_channel"
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title ?: "알림")
            .setContentText(message ?: "")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 이상은 채널 등록 필수
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel) // 이미 있으면 무시됨
        }


        notificationManager.notify(0, builder.build())
    }
}
