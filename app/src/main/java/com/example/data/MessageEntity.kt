package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assistant_messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val request: String,
    val response: String,
    val detectedLanguage: String,
    val actionExecuted: String,
    val timestamp: Long = System.currentTimeMillis()
)
