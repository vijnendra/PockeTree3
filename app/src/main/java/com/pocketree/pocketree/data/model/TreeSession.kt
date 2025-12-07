package com.pocketree.pocketree.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tree_sessions")
data class TreeSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val plannedMinutes: Int,
    val durationMinutes: Int,
    val startTime: Long,
    val endTime: Long,
    val isWithered: Boolean,
    val treeType: String = "default",
    val xp: Int = 0,
    val notes: String = ""
)
