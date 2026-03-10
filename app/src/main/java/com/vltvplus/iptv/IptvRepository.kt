package com.vltvplus.iptv

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.zip.GZIPInputStream

class IptvRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val movieDao = db.movieDao()
    private val seriesDao = db.seriesDao()
    private val client = OkHttpClient()
    private val gson = Gson()

    // Sincroniza Filmes e Séries simultaneamente para abrir a Home completa
    suspend fun sincronizarConteudoTotal(dns: String, user: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Dispara os dois downloads ao mesmo tempo (Modo Turbo)
            val jobFilmes = async { sincronizarFilmes(dns, user, pass) }
            val jobSeries = async { sincronizarSeries(dns, user, pass) }

            // Retorna verdadeiro apenas se os dois terminarem com sucesso
            return@withContext jobFilmes.await() && jobSeries.await()
        } catch (e: Exception) {
            Log.e("IPTV_REPO", "Erro na sincronização total: ${e.message}")
            false
        }
    }

    private suspend fun sincronizarFilmes(dns: String, user: String, pass: String): Boolean {
        return try {
            val url = "$dns/player_api.php?username=$user&password=$pass&action=get_vod_streams"
            val request = Request.Builder().url(url).addHeader("Accept-Encoding", "gzip").build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return false

                val source = response.body?.source()
                val inputStream = if (response.header("Content-Encoding") == "gzip") {
                    GZIPInputStream(source?.inputStream())
                } else {
                    source?.inputStream()
                }

                val reader = inputStream?.bufferedReader()
                val listType = object : TypeToken<List<IptvMovie>>() {}.type
                val movies: List<IptvMovie> = gson.fromJson(reader, listType)

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

                movieDao.clearAll()
                movieDao.insertAll(entities)
                Log.d("IPTV_REPO", "Filmes sincronizados: ${entities.size}")
                true
            }
        } catch (e: Exception) {
            Log.e("IPTV_REPO", "Erro nos filmes: ${e.message}")
            false
        }
    }

    private suspend fun sincronizarSeries(dns: String, user: String, pass: String): Boolean {
        return try {
            // URL específica para Séries no padrão Xtreme Codes
            val url = "$dns/player_api.php?username=$user&password=$pass&action=get_series"
            val request = Request.Builder().url(url).addHeader("Accept-Encoding", "gzip").build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return false

                val source = response.body?.source()
                val inputStream = if (response.header("Content-Encoding") == "gzip") {
                    GZIPInputStream(source?.inputStream())
                } else {
                    source?.inputStream()
                }

                val reader = inputStream?.bufferedReader()
                // Aqui usamos o modelo de IptvSeries que mapeia os campos do seu servidor
                val listType = object : TypeToken<List<IptvSeries>>() {}.type
                val series: List<IptvSeries> = gson.fromJson(reader, listType)

                val entities = series.map { 
                    SeriesEntity(
                        seriesId = it.seriesId.toInt(),
                        name = it.name,
                        seriesIdString = it.seriesId,
                        cover = it.lastModified, // O servidor costuma enviar a capa aqui ou no stream_icon
                        plot = it.plot,
                        cast = it.cast,
                        director = it.director,
                        genre = it.genre,
                        releaseDate = it.releaseDate,
                        lastModified = it.lastModified,
                        rating = it.rating,
                        categoryId = it.categoryId ?: "0"
                    )
                }

                seriesDao.clearAll()
                seriesDao.insertAll(entities)
                Log.d("IPTV_REPO", "Séries sincronizadas: ${entities.size}")
                true
            }
        } catch (e: Exception) {
            Log.e("IPTV_REPO", "Erro nas séries: ${e.message}")
            false
        }
    }
}
