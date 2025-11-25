package org.ufku.ii12task


import ai.z.openapi.ZaiClient
import ai.z.openapi.service.model.ChatCompletionCreateParams
import ai.z.openapi.service.model.ChatCompletionResponse
import ai.z.openapi.service.model.ChatMessage
import ai.z.openapi.service.model.ChatMessageRole
import ai.z.openapi.service.model.ChatThinking
import ai.z.openapi.service.model.ChatThinkingType
import ai.z.openapi.service.model.ChatTool
import androidx.compose.runtime.remember
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class Zai() {
    private val apiKey = dotenv().get("API_KEY")


    private val zaiClient: ZaiClient = ZaiClient.builder()
        .apiKey(apiKey)
        .baseUrl("https://api.z.ai/api/coding/paas/v4/")
        .enableTokenCache()
        .tokenExpire(3600000) // 1 hour
        .connectionPool(10, 5, TimeUnit.MINUTES)
        .build()

    private val systemMessage = ChatMessage.builder()
        .role(ChatMessageRole.SYSTEM.value())
        .content("Ты опытный пользователь binance и даешь советы новичкам")
        .build()

    private fun createUserMessage() = ChatMessage.builder()
        .role(ChatMessageRole.USER.value())
        .content("Найди лучший тикер среди следующих на binance")
        .build()

    private fun createUserMessageRag(contextTexts: List<String>): ChatMessage {
        val contextBlock = contextTexts.mapIndexed { index, text ->
            "[${index + 1}] $text"
        }.joinToString("\n\n")

        val prompt = buildString {
            append("Контекст:\n\n")
            append(contextBlock)
            append("\n\nИспользуя только этот контекст, ответь на вопрос: \"")
            append("Найди лучший тикер среди следующих на binance")
            append("\"")
        }

        return ChatMessage.builder()
            .role(ChatMessageRole.USER.value())
            .content(prompt)
            .build()
    }

    suspend fun invokeRequest(): String {
        val userMessage = createUserMessage()

        val request = createUserRequest(userMessage)
        val response = withContext(Dispatchers.IO) {
            zaiClient.chat().createChatCompletion(request)
        }

        return if (response.isSuccess) {
            val text = (response.data.choices[0].message.content) as? String ?: ""

            text
        } else {
            response.msg
        }
    }

    suspend fun invokeRequestRag(data: List<String>): String {
        val userMessage = createUserMessageRag(data)

        val request = createUserRequest(userMessage)
        val response = withContext(Dispatchers.IO) {
            zaiClient.chat().createChatCompletion(request)
        }

        return if (response.isSuccess) {
            val text = (response.data.choices[0].message.content) as? String ?: ""

            text
        } else {
            response.msg
        }
    }

    private fun createUserRequest(userMessage: ChatMessage) : ChatCompletionCreateParams {
        return ChatCompletionCreateParams.builder()
            .model("glm-4.6")
            .messages(listOf(systemMessage, userMessage))
            .temperature(0.5f)
            .maxTokens(4096)
            .thinking(ChatThinking.builder().type(ChatThinkingType.DISABLED.value()).build())
            .build()
    }
}