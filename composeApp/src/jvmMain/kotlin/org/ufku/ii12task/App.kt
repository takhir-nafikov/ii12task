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
            val rch = ollama.embed("Найди лучший тикер среди следующих на binance")

            val list1 = ollama.readTopKChunksFromJson(rch.embedding, false)
            responseText = "отправка простого запроса без фильтра"
            responseText = zai.invokeRequestRag(list1)
            delay(10000)

            responseText = "отправка с фильтром"
            val list = ollama.readTopKChunksFromJson(rch.embedding)
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