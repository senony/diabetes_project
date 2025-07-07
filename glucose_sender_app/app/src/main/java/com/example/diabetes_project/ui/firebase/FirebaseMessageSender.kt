package com.example.diabetes_project.ui.firebase

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseMessageSender {
    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("messages")

    fun sendMessage(message: String) {
        val data = mapOf("content" to message)
        database.push().setValue(data)
    }
}