package com.delta.harmonica.model

import kotlinx.serialization.Serializable

@Serializable
data class Score(
    val title: String = "新乐谱",
    val artist: String? = null,
    val mode: String = "8+4",
    val key: String = "C",
    val bpm: Int = 80,
    val content: String = "",
    val lyric: String? = null,
    val keymap: Map<String, String> = mapOf(
        "1" to "1",
        "2" to "2",
        "3" to "3",
        "4" to "4",
        "5" to "5",
        "6" to "6",
        "7" to "7",
        "i" to "i",
        "#1" to "#1",
        "#2" to "#2",
        "#4" to "#4",
        "#5" to "#5"
    )
)
