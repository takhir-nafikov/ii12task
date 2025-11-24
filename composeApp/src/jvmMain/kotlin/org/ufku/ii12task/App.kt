package org.ufku.ii12task

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
        var responseText by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            val chunks = ollama.readChunkFromFolder()
            chunks.forEach {
                responseText = "получаем данные"
                val array = ollama.embed(it)
                responseText = "пишем данные"
                ollama.writeEmbeddingToJsonFile(array)
            }
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