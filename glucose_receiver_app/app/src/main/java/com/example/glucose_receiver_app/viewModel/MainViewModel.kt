package com.example.glucose_receiver_app.viewModel

import androidx.lifecycle.ViewModel
import com.example.glucose_receiver_app.data.GlucoseLog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {
    private val _logs = MutableStateFlow<List<GlucoseLog>>(emptyList())
    val logs: StateFlow<List<GlucoseLog>> = _logs

    init {
        loadLogs()
    }

    private fun loadLogs() {
        FirebaseFirestore.getInstance()
            .collection("glucose_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                val list = snapshots?.documents?.mapNotNull { doc ->
                    val glucose = doc.getLong("glucose")?.toInt()
                    val timestamp = doc.getLong("timestamp")
                    if (glucose != null && timestamp != null) {
                        GlucoseLog(glucose, timestamp)
                    } else null
                } ?: emptyList()
                _logs.value = list
            }
    }
}