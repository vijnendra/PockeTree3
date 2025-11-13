package com.pocketree.pocketree.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val durationMinutes: Int,
    val timestamp: Long,
    val completed: Boolean,
    val treeType: String? = null
)
