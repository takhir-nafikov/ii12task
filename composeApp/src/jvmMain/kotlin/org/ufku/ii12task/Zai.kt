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

    private fun createUserMessage(text: String) = ChatMessage.builder()
        .role(ChatMessageRole.USER.value())
        .content("Найди лучший тикер среди следующих - $text")
        .build()

    private fun createUserMessage2(text: String) = ChatMessage.builder()
        .role(ChatMessageRole.USER.value())
        .content("Сделай выжимку по тикерам из этих ответов - $text")
        .build()

    suspend fun invokeRequest(userText: String): String {
        val userMessage = createUserMessage(userText)

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

    suspend fun invokeRequest2(userText: String): String {
        val userMessage = createUserMessage2(userText)

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