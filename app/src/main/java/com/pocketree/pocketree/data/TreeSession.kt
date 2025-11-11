package com.pocketree.pocketree.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tree_sessions")
data class TreeSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val durationSeconds: Long,  // Timer duration
    val timestamp: Long,        // Session timestamp
    val coinsEarned: Int        // Gamification coins
)
