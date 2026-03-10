package com.vltvplus.iptv

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.zip.GZIPInputStream

class IptvRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val movieDao = db.movieDao()
    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun sincronizarFilmes(dns: String, user: String, pass: String) = withContext(Dispatchers.IO) {
        try {
            // URL padrão do Xtreme Codes para Filmes (VOD)
            val url = "$dns/player_api.php?username=$user&password=$pass&action=get_vod_streams"
            
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept-Encoding", "gzip") // Solicita compressão Gzip
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext

                val source = response.body?.source()
                val inputStream = if (response.header("Content-Encoding") == "gzip") {
                    GZIPInputStream(source?.inputStream())
                } else {
                    source?.inputStream()
                }

                val reader = inputStream?.bufferedReader()
                val listType = object : TypeToken<List<IptvMovie>>() {}.type
                val movies: List<IptvMovie> = gson.fromJson(reader, listType)

                // Converte os dados da API para o formato do nosso Banco de Dados Room
                val entities = movies.map { 
                    MovieEntity(
                        streamId = it.streamId,
                        name = it.name,
                        containerExtension = it.containerExtension,
                        categoryId = it.categoryId ?: "0",
                        streamIcon = it.streamIcon,
                        rating = it.rating
                    )
                }

                // Salva tudo no banco de dados em lote (Muito rápido)
                movieDao.clearAll()
                movieDao.insertAll(entities)
                
                Log.d("IPTV_REPO", "Sincronização concluída: ${entities.size} filmes salvos.")
            }
        } catch (e: Exception) {
            Log.e("IPTV_REPO", "Erro no download turbo: ${e.message}")
        }
    }
}
