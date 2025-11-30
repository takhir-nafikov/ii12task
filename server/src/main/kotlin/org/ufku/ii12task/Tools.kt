package org.ufku.ii12task

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

var count = 0
suspend fun HttpClient.getTickers(count: Int): List<String> {
    val uri = "/api/v3/ticker/24hr"
    // Request the alerts data from the API
    val alerts = this.get(uri).body<List<TickerInfo>>()

    return alerts.take(count).map { it.toString() }
}

fun saveToFile(data: String): Boolean {
    return try {
        // Папка для сохранения
        val folder = File("markdown_files")

        // Создаём папку, если её нет
        if (!folder.exists()) {
            folder.mkdirs()
        }


        val file = File(folder, "note_$count.md")
        count++

        // Записываем текст
        file.writeText(data)

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

suspend fun readFromFile(): String = coroutineScope {
    val dir = File("markdown_files")

    require(dir.exists() && dir.isDirectory) { "Directory does not exist: markdown_files" }

    val mdFiles = mutableListOf<File>()

    // Обычный цикл для поиска .md файлов
    for (file in dir.listFiles() ?: emptyArray()) {
        if (file.isFile && file.extension.lowercase() == "md") {
            mdFiles.add(file)
        }
    }

    // Асинхронное чтение каждого файла
    val deferredContents = mdFiles.map { file ->
        async(Dispatchers.IO) {
            file.readText()
        }
    }

    // Ждём все чтения и объединяем строки
    deferredContents.awaitAll().joinToString("\n")
}

@Serializable
data class TickerInfo(
    val symbol: String,
    val priceChange: String,
    val priceChangePercent: String,
    val weightedAvgPrice: String,
    val prevClosePrice: String,
    val lastPrice: String,
    val lastQty: String,
    val bidPrice: String,
    val bidQty: String,
    val askPrice: String,
    val askQty: String,
    val openPrice: String,
    val highPrice: String,
    val lowPrice: String,
    val volume: String,
    val quoteVolume: String,
    val openTime: Long,
    val closeTime: Long,
    val firstId: Long,
    val lastId: Long,
    val count: Int
)