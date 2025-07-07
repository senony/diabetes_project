package com.example.glucose_receiver_app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.glucose_receiver_app.ui.theme.Glucose_receiver_appTheme
import com.example.glucose_receiver_app.viewModel.MainViewModel
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "현재 토큰: $token")
                Toast.makeText(this, "FCM 토큰:\n$token", Toast.LENGTH_LONG).show()
            } else {
                Log.e("FCM", "토큰 가져오기 실패", task.exception)
            }
        }

        setContent {
            Glucose_receiver_appTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GlucoseLogList(viewModel)
                }
            }
        }
    }
}

@Composable
fun GlucoseLogList(viewModel: MainViewModel) {
    val logs by viewModel.logs.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "수신된 혈당 로그 (${logs.size}개)",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(logs) { log ->
                val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(log.timestamp))
                Text(
                    text = "혈당: ${log.glucose} mg/dL @ $formattedTime",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}