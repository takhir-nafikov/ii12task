package org.ufku.ii12task

import ai.z.openapi.ZaiClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val ollama = OllamaClient()
        val zai = Zai()
        var responseText by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            responseText = "читаем"
            val ch = ollama.readChunksFromFolder()
            responseText = "начинаем запись"
            ch.forEach {
                val ar = ollama.embed(it)
                ollama.writeEmbeddingToJsonFile(Chunk(text = it.chunk, embedding = ar.embedding, fileName = it.fileName))
            }
            responseText = "закончили запись"
            val rch = ollama.embed("Найди лучший вариант тикера среди следующих")

            val list = ollama.readTopKChunksFromJson(rch.embedding)
            responseText = "отправка запроса"
            responseText = zai.invokeRequestRag(list)
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = responseText,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}