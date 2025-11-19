package org.ufku.ii12task

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import ii12task.composeapp.generated.resources.Res
import ii12task.composeapp.generated.resources.compose_multiplatform
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    MaterialTheme {
        val client = Client()
        val zai = Zai()
        var responseText by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            client.connect()
            while (true) {
                responseText = "ждем ответ"
                val tickers = client.callTool(20)
                if (tickers.isNullOrEmpty()) {
                    responseText = "Нет тикеров"
                } else {
                    val res = zai.invokeRequest(tickers.joinToString(";"))
                    responseText = res
                }
                delay(20_000L)
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