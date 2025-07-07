package com.example.glucose_receiver_app.firebase

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.glucose_receiver_app.data.GlucoseLog
import com.example.glucose_receiver_app.util.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.HttpURLConnection
import java.net.URL

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val url = "http://10.0.2.2:3000/register" // 에뮬레이터용. 실제 디바이스는 서버 IP 주소로 교체

        val json = """
            {
              "token": "$token"
            }
        """.trimIndent()

        Thread {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.doOutput = true

                conn.outputStream.use {
                    it.write(json.toByteArray(Charsets.UTF_8))
                }

                val response = conn.responseCode
                Log.d("FCM", "서버 응답 코드: $response")
            } catch (e: Exception) {
                Log.e("FCM", "토큰 전송 실패", e)
            }
        }.start()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "📩 FCM 수신됨: ${remoteMessage.data}")
        super.onMessageReceived(remoteMessage)

        val glucoseStr = remoteMessage.data["glucose"]
        val glucose = glucoseStr?.toIntOrNull()
        val title = remoteMessage.data["title"] ?: "혈당 알림"
        val body = remoteMessage.data["body"] ?: "혈당: $glucoseStr mg/dL"

        Log.d("FCM", "수신된 데이터 - glucose: $glucoseStr")

        // 1. Notification 표시
        showNotification(title, body)

        // 2. Firestore 저장
        if (glucose != null) {
            val log = GlucoseLog(glucose = glucose)
            FirebaseFirestore.getInstance()
                .collection("glucose_logs")
                .add(log)
                .addOnSuccessListener {
                    Log.d("FCM", "혈당 로그 저장 성공")
                }
                .addOnFailureListener {
                    Log.e("FCM", "혈당 로그 저장 실패", it)
                }
        }
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = "glucose_channel"

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.star_on)
            .setContentTitle(title ?: "알림")
            .setContentText(message ?: "")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Glucose Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        manager.notify(0, builder.build())
    }
}