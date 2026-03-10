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

    // Agora a função retorna um booleano para confirmar o preenchimento do banco
    suspend fun sincronizarFilmes(dns: String, user: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = "$dns/player_api.php?username=$user&password=$pass&action=get_vod_streams"
            
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept-Encoding", "gzip") 
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext false

                val source = response.body?.source()
                val inputStream = if (response.header("Content-Encoding") == "gzip") {
                    GZIPInputStream(source?.inputStream())
                } else {
                    source?.inputStream()
                }

                val reader = inputStream?.bufferedReader()
                val listType = object : TypeToken<List<IptvMovie>>() {}.type
                val movies: List<IptvMovie> = gson.fromJson(reader, listType)

                // Mapeamento para o Banco de Dados Room
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

                // Limpa o lixo anterior e salva a nova lista
                movieDao.clearAll()
                movieDao.insertAll(entities)
                
                Log.d("IPTV_REPO", "Sincronização concluída: ${entities.size} filmes salvos.")
                true // Retorna sucesso para o LoginFragment
            }
        } catch (e: Exception) {
            Log.e("IPTV_REPO", "Erro no download turbo: ${e.message}")
            false // Retorna falha
        }
    }
}
