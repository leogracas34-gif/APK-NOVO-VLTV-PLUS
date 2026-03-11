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
                        if (response.isSuccessful) {
                            dns to response.body()
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            
            for (job in jobs) {
                val result = job.await()
                if (result != null) {
                    // Cancelar outros jobs
                    jobs.forEach { it.cancel(CancellationException("DNS encontrado")) }
                    return@coroutineScope result
                }
            }
            null
        }
    }
    
    private fun createService(baseUrl: String): XtreamService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XtreamService::class.java)
    }
}
