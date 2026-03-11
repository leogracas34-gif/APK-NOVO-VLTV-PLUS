package com.vltv.plus.data.network

import com.vltv.plus.data.network.model.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.CancellationException

object DnsManager {
    
    suspend fun findWorkingDns(
        username: String, 
        password: String
    ): Pair<String, LoginResponse>? = withContext(Dispatchers.IO) {
        coroutineScope {
            val jobs = DnsConfig.DNS_LIST.map { dns ->
                async {
                    try {
                        val service = createService(dns)
                        val response = service.getProfile(username, password)
                        val body = response.body()
                        
                        // Garante que o DNS e o Body não são nulos antes de criar o Pair
                        if (response.isSuccessful && body != null) {
                            dns to body
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            
            var foundResult: Pair<String, LoginResponse>? = null
            
            for (job in jobs) {
                val result = job.await()
                if (result != null) {
                    foundResult = result
                    // Cancela as outras tentativas de DNS para economizar recursos
                    jobs.forEach { if (it != job) it.cancel(CancellationException("DNS encontrado")) }
                    break
                }
            }
            foundResult
        }
    }
    
    private fun createService(baseUrl: String): XtreamService {
        // Garante que a URL termine com '/' para o Retrofit não dar erro
        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XtreamService::class.java)
    }
}
