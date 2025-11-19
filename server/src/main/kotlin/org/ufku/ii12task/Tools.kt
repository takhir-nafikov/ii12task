package org.ufku.ii12task

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

suspend fun HttpClient.getTickers(count: Int): List<String> {
    val uri = "/api/v3/ticker/24hr"
    // Request the alerts data from the API
    val alerts = this.get(uri).body<List<TickerInfo>>()

    return alerts.take(count).map { it.toString() }
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