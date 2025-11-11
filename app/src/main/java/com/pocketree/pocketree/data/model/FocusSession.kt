package com.pocketree.pocketree.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTs: Long,
    val endTs: Long,
    val durationSec: Int,
    val completed: Boolean,
    val treeType: String?
)
