package com.pocketree.pocketree.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val durationMinutes: Int,
    val wasWithered: Boolean,
    val dayKey: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
)
