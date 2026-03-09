package com.vltvplus.iptv.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.cancelChildren

class DnsRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val dnsList = listOf(
        "http://fibercdn.sbs",
        "http://tvblack.shop",
        "http://redeinternadestiny.top",
        "http://blackstartv.shop",
        "http://blackdns.shop",
        "http://blackdeluxe.shop",
        "http://ranos.sbs",
        "http://cmdtv.casa",
        "http://cmdtv.pro",
        "http://cmdtv.sbs",
        "http://cmdtv.top",
        "http://cmdbr.life"
    )

    suspend fun authenticateAndFindDns(username: String, password: String): String? = withContext(Dispatchers.IO) {
        var winnerUrl: String? = null

        try {
            coroutineScope {
                val tasks = dnsList.map { url ->
                    async {
                        val isSuccess = testLogin(url, username, password)
                        if (isSuccess) {
                            winnerUrl = url
                            this@coroutineScope.coroutineContext.cancelChildren()
                        }
                    }
                }
                tasks.awaitAll()
            }
        } catch (e: Exception) {
            // Exceção ignorada pois o cancelamento é intencional ao achar o vencedor
        }

        return@withContext winnerUrl
    }

    private fun testLogin(baseUrl: String, username: String, password: String): Boolean {
        val targetUrl = "$baseUrl/player_api.php?username=$username&password=$password"

        val request = Request.Builder()
            .url(targetUrl)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    responseBody.contains("user_info")
                } else {
                    false
                }
            }
        } catch (e: IOException) {
            false
        }
    }
}
