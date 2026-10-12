package com.delta.harmonica.parser

import com.delta.harmonica.model.Score

data class PlaybackEvent(
    val noteIndex: Int,
    val durationMs: Long
)

object ScoreParser {
    private val defaultMap = mapOf(
        "1" to 0,
        "2" to 1,
        "3" to 2,
        "4" to 3,
        "5" to 4,
        "6" to 5,
        "7" to 6,
        "i" to 7,
        "#1" to 8,
        "#2" to 9,
        "#4" to 10,
        "#5" to 11
    )

    private val aliasMap = mapOf(
        "z" to "1",
        "x" to "2",
        "c" to "3",
        "v" to "4",
        "b" to "5",
        "n" to "6",
        "m" to "7",
        "," to "i",
        "Z" to "#1",
        "X" to "#2",
        "V" to "#4",
        "B" to "#5"
    )

    fun parse(score: Score): List<PlaybackEvent> {
        val tokens = score.content
            .replace("|", " ")
            .replace("·", " ")
            .replace("—", " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        val events = mutableListOf<PlaybackEvent>()

        for (token in tokens) {
            val noteToken = token.trim()
            val durationFactor = when {
                noteToken.contains(".") -> 1.5
                noteToken.contains("..") -> 2.0
                else -> 1.0
            }

            val clean = noteToken
                .replace(".", "")
                .replace("。", "")

            if (clean == "-" || clean == "休" || clean == "0") {
                continue
            }

            val resolved = resolveNote(clean, score.keymap)
            if (resolved == null) continue

            val durationMs = (60000.0 / score.bpm * durationFactor).toLong()
            events.add(PlaybackEvent(resolved, durationMs))
        }

        return events
    }

    private fun resolveNote(token: String, keymap: Map<String, String>): Int? {
        val mapped = keymap[token] ?: aliasMap[token] ?: token
        val finalToken = if (mapped in defaultMap) mapped else aliasMap[mapped] ?: mapped
        return defaultMap[finalToken]
    }
}
