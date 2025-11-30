package org.ufku.ii12task

import ai.z.openapi.ZaiClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        // Лучше создавать такие объекты через remember,
        // чтобы они не пересоздавались при каждом recomposition
        val ollama = remember { OllamaClient() }
        val zai = remember { Zai() }
        val client = remember { Client() }

        var responseText by remember { mutableStateOf("") }
        var inputText by remember { mutableStateOf("") }

        val scrollState = rememberScrollState()
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Область с сообщениями
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    text = if (responseText.isEmpty()) "Пока сообщений нет" else responseText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Поле ввода + кнопка
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Введите сообщение") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (inputText.isBlank()) return@Button

                        val userMessage = inputText
                        inputText = ""

                        scope.launch {
                            val answer: String = try {
                                val ragReq = ollama.embed(userMessage)
                                val topChunk = ollama.readTopKChunksFromJson(ragReq.embedding)
                                zai.invokeRequestRag(topChunk)
                            } catch (e: Exception) {
                                "Ошибка: ${e.message}"
                            }

                            // Обновляем "историю" диалога
                            responseText +=
                                "\n\nВы: $userMessage\nБот: $answer"
                        }
                    }
                ) {
                    Text("Отправить")
                }
            }
        }
    }
}
