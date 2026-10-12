package com.delta.harmonica.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String?,
    val mode: String,
    val key: String,
    val bpm: Int,
    val content: String,
    val lyric: String?,
    val keymapJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
