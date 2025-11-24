package org.ufku.ii12task

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LoggingFormat
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File


class OllamaClient(): AutoCloseable {
    val httpClient = HttpClient(CIO) {
        defaultRequest {
            headers {
                append("Accept", "application/json")
            }
            contentType(ContentType.Application.Json)
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    encodeDefaults = true
                },
            )
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun embed(chunkText: String): FloatArray = withContext(Dispatchers.IO) {
        val response: OllamaEmbeddingsResponse = httpClient.post("http://localhost:11434/api/embeddings") {
            contentType(ContentType.Application.Json)
            setBody(OllamaEmbeddingsRequest(prompt = chunkText))
        }.body()
        response.embedding.map { it.toFloat() }.toFloatArray()
    }

    override fun close() {
        httpClient.close()
    }

    suspend fun writeEmbeddingToJsonFile(
        array: FloatArray
    ) = withContext(Dispatchers.IO) {
        val file = File("embed.json")
        // Если файл не существует — создаём его с пустым JSON-массивом
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.writeText("[]")
        }

        // Загружаем уже существующие данные
        val existingList = json.decodeFromString<MutableList<EmbeddingWrapper>>(file.readText())

        // Добавляем новое значение
        existingList.add(EmbeddingWrapper(array.toList()))

        // Сохраняем обратно
        file.writeText(json.encodeToString(existingList))
    }

    suspend fun readChunkFromFolder(): List<String> = coroutineScope {
        val dir = File("F:\\Repos\\ii12task\\markdown_files")

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
        val text = deferredContents.awaitAll().joinToString("\n")

        val chunkSize: Int = 100
        val overlap: Int = 15

        val result = mutableListOf<String>()
        var start = 0

        while (start < text.length) {
            val end = (start + chunkSize).coerceAtMost(text.length)

            // Основная часть
            val core = text.substring(start, end)

            // Добавляем overlap с предыдущим и следующим блоком
            val prefixStart = (start - overlap).coerceAtLeast(0)
            val prefix = text.substring(prefixStart, start)

            val suffixEnd = (end + overlap).coerceAtMost(text.length)
            val suffix = text.substring(end, suffixEnd)

            result.add(prefix + core + suffix)
            start += chunkSize
        }

        result
    }
}

@Serializable
data class OllamaEmbeddingsRequest(
    @SerialName("model")
    val model: String = "nomic-embed-text",
    @SerialName("prompt")
    val prompt: String
)

@Serializable
data class OllamaEmbeddingsResponse(
    val embedding: List<Double>
)

@Serializable
data class EmbeddingWrapper(
    val embedding: List<Float>
)