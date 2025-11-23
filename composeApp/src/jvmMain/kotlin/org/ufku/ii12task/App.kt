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
        val client = Client()
        val client2 = Client2()
        val zai = Zai()
        var responseText by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            client.connect()
            client2.connect()

            for(i in 0 until 3) {
                responseText = "ждем ответ"
                val tickers = client.callTool(10)
                if (tickers.isNullOrEmpty()) {
                    responseText = "Нет тикеров"
                } else {
                    val res = zai.invokeRequest(tickers.joinToString(";"))
                    responseText = "сохраняем в файл"
                    val saveRes = client.saveTool(res)
                    responseText = saveRes
                }
                delay(5_000L)
            }

            responseText = "читаем все что есть"
            val allData = client2.readTool()
            responseText = if (allData.isNullOrEmpty()) {
                "чет не прочли"
            } else {
                zai.invokeRequest2(allData)
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