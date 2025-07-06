package com.example.diabetes_project

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.diabetes_project.ui.firebase.FirebaseMessageSender
import com.example.diabetes_project.ui.screen.MessageInputScreen
import com.example.diabetes_project.ui.theme.Diabetes_projectTheme
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }


        database = FirebaseDatabase.getInstance().reference.child("messages")
        FirebaseMessaging.getInstance().subscribeToTopic("allUsers")

        // 🔽 FCM 토큰 가져오기 및 출력
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("❌ FCM 토큰 가져오기 실패: ${task.exception}")
                return@addOnCompleteListener
            }

            // ✅ 토큰 출력
            val token = task.result
            println("✅ FCM 토큰: $token")
        }

        setContent {
            Diabetes_projectTheme {
                MessageInputScreen { message ->
                    FirebaseMessageSender.sendMessage(message)
                }
            }
        }
    }
}
