package com.delta.harmonica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.delta.harmonica.audio.TonePlayer
import com.delta.harmonica.data.AppDatabase
import com.delta.harmonica.data.ScoreEntity
import com.delta.harmonica.model.Score
import com.delta.harmonica.parser.ScoreParser
import com.delta.harmonica.ui.DeltaHarmonicaTheme
import com.delta.harmonica.ui.HarmonicaBoard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    private val json = Json { prettyPrint = true }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getInstance(this)

        setContent {
            DeltaHarmonicaTheme {
                val scores by db.scoreDao().getAll().collectAsState(initial = emptyList())
                AppScreen(db = db, scores = scores, json = json)
            }
        }
    }
}

@Composable
fun AppScreen(
    db: AppDatabase,
    scores: List<ScoreEntity>,
    json: Json
) {
    val appScope = remember { CoroutineScope(Dispatchers.Main + SupervisorJob()) }
    val tonePlayer = remember { TonePlayer() }

    var bpm by remember { mutableIntStateOf(80) }
    var highlighted by remember { mutableStateOf<Int?>(null) }
    var showJsonDialog by remember { mutableStateOf(false) }
    var jsonText by remember { mutableStateOf(defaultJsonExample()) }
    var selectedTitle by remember { mutableStateOf("示例乐谱") }
    var previewRunning by remember { mutableStateOf(false) }
    var previewJob: Job? by remember { mutableStateOf(null) }

    fun stopPlayback() {
        previewRunning = false
        previewJob?.cancel()
        highlighted = null
    }

    fun startPreview(score: Score) {
        stopPlayback()
        val events = ScoreParser.parse(score)
        if (events.isEmpty()) return

        previewRunning = true
        previewJob = appScope.launch {
            for (event in events) {
                if (!previewRunning) break
                highlighted = event.noteIndex
                tonePlayer.play(event.noteIndex, 180)
                delay(event.durationMs)
            }
            previewRunning = false
            highlighted = null
        }
    }

    fun insertScore(score: Score) {
        val entity = ScoreEntity(
            title = score.title,
            artist = score.artist,
            mode = score.mode,
            key = score.key,
            bpm = score.bpm,
            content = score.content,
            lyric = score.lyric,
            keymapJson = json.encodeToString(score.keymap)
        )
        appScope.launch {
            db.scoreDao().insert(entity)
        }
        selectedTitle = score.title
    }

    LaunchedEffect(Unit) {
        val list = db.scoreDao().getAll().first()
        if (list.isEmpty()) {
            val example = Score(
                title = "示例乐谱",
                artist = "本地示例",
                mode = "8+4",
                key = "C",
                bpm = 80,
                content = "z z x c v b n m | z x c v b n m , |",
                lyric = "示例乐谱",
                keymap = mapOf(
                    "z" to "1", "x" to "2", "c" to "3", "v" to "4",
                    "b" to "5", "n" to "6", "m" to "7", "," to "i"
                )
            )
            insertScore(example)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010))
            .padding(12.dp)
    ) {
        Text(
            text = "三角洲口琴练习器",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFFFFD58A)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "当前乐谱：$selectedTitle",
            color = Color(0xFFE6D8A8)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            items(scores) { item ->
                Button(
                    onClick = {
                        selectedTitle = item.title
                        bpm = item.bpm
                        stopPlayback()
                        val parsed = Score(
                            title = item.title,
                            artist = item.artist,
                            mode = item.mode,
                            key = item.key,
                            bpm = item.bpm,
                            content = item.content,
                            lyric = item.lyric,
                            keymap = try {
                                json.decodeFromString<Map<String, String>>(item.keymapJson)
                            } catch (_: Exception) {
                                emptyMap()
                            }
                        )
                        startPreview(parsed)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(item.title)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        HarmonicaBoard(
            highlightedIndex = highlighted,
            onKeyClick = { index ->
                highlighted = index
                tonePlayer.play(index, 220)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "BPM: $bpm",
            color = Color(0xFFFFD58A)
        )
        Slider(
            value = bpm.toFloat(),
            onValueChange = { bpm = it.toInt() },
            valueRange = 40f..180f
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                val score = Score(
                    title = selectedTitle.ifBlank { "示例乐谱" },
                    artist = "本地练习",
                    mode = "8+4",
                    key = "C",
                    bpm = bpm,
                    content = "z z x c v b n m | z x c v b n m , |",
                    lyric = "演示",
                    keymap = mapOf(
                        "z" to "1", "x" to "2", "c" to "3", "v" to "4",
                        "b" to "5", "n" to "6", "m" to "7", "," to "i"
                    )
                )
                startPreview(score)
            }) {
                Text("开始预览")
            }

            Button(onClick = { stopPlayback() }) {
                Text("暂停")
            }

            Button(onClick = {
                stopPlayback()
                val score = Score(
                    title = selectedTitle.ifBlank { "示例乐谱" },
                    artist = "本地练习",
                    mode = "8+4",
                    key = "C",
                    bpm = bpm,
                    content = "z z x c v b n m | z x c v b n m , |",
                    lyric = "重放",
                    keymap = mapOf(
                        "z" to "1", "x" to "2", "c" to "3", "v" to "4",
                        "b" to "5", "n" to "6", "m" to "7", "," to "i"
                    )
                )
                startPreview(score)
            }) {
                Text("重播")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { showJsonDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("导入 JSON 乐谱")
        }

        if (showJsonDialog) {
            Dialog(onDismissRequest = { showJsonDialog = false }) {
                Column(
                    modifier = Modifier
                        .background(Color(0xFF1D1D1D))
                        .padding(16.dp)
                ) {
                    Text("粘贴乐谱 JSON", color = Color(0xFFFFD58A))
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = jsonText,
                        onValueChange = { jsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = { showJsonDialog = false }) {
                            Text("取消")
                        }

                        Button(onClick = {
                            try {
                                val score = json.decodeFromString<Score>(jsonText)
                                insertScore(score)
                                selectedTitle = score.title
                                bpm = score.bpm
                                showJsonDialog = false
                            } catch (_: Exception) {
                                // 可补 Toast / 错误提示
                            }
                        }) {
                            Text("保存")
                        }
                    }
                }
            }
        }
    }
}

private fun defaultJsonExample(): String {
    return """
        {
          "title": "示例乐谱",
          "artist": "本地示例",
          "mode": "8+4",
          "key": "C",
          "bpm": 80,
          "content": "z z x c v b n m | z x c v b n m , |",
          "lyric": "示例",
          "keymap": {
            "1":"1",
            "2":"2",
            "3":"3",
            "4":"4",
            "5":"5",
            "6":"6",
            "7":"7",
            "i":"i",
            "#1":"#1",
            "#2":"#2",
            "#4":"#4",
            "#5":"#5"
          }
        }
    """.trimIndent()
}
