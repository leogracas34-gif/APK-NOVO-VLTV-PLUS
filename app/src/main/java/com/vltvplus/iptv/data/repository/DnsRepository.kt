package com.vltvplus.iptv.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.dnsoverhttps.DnsOverHttps
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.cancelChildren

class DnsRepository {

    // 1. Cliente Base para o DoH (DNS over HTTPS)
    private val bootstrapClient = OkHttpClient.Builder().build()

    // 2. Configuração do DoH (Cloudflare) para evitar bloqueio de operadora (Claro, Vivo, etc.)
    private val dnsOverHttps = DnsOverHttps.Builder()
        .client(bootstrapClient)
        .url("https://cloudflare-dns.com/dns-query".toHttpUrl())
        .bootstrapDnsHosts(InetAddress.getByName("1.1.1.1"), InetAddress.getByName("1.0.0.1"))
        .build()

    // 3. Interceptor para Gzip Extremo e Anti-Bloqueio (User-Agent Spoofing)
    private val extremeNetworkInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        
        val newRequest = originalRequest.newBuilder()
            // Camufla o aplicativo como se fosse um Navegador comum
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            // Força a compressão Gzip para baixar listas pesadas muito mais rápido
            .header("Accept-Encoding", "gzip")
            .build()
            
        chain.proceed(newRequest)
    }

    // 4. Cliente OkHttp Oficial Turbinado
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .dns(dnsOverHttps) // Aplica o Anti-Bloqueio de Operadora
        .addInterceptor(extremeNetworkInterceptor) // Aplica o Gzip e a Camuflagem
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
