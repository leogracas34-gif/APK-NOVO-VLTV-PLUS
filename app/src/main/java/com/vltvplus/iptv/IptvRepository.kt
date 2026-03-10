package com.vltvplus.iptv

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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

    // FUNÇÃO PRINCIPAL: Faz a carga rápida para liberar a Home e continua o resto em background
    suspend fun sincronizarConteudoTotal(dns: String, user: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Verifica se já temos dados. Se tiver, libera a Home na hora (0 segundos de espera)
            val temDados = movieDao.getCount() > 0 || seriesDao.getCount() > 0
            
            if (temDados) {
                // Dispara a atualização silenciosa em background e libera a Home
                launch { atualizarTudo(dns, user, pass) }
                return@withContext true
            }

            // Se for a primeira vez (banco vazio), faz a carga prioritária (2 segundos)
            val jobFilmes = async { sincronizarFilmes(dns, user, pass, priority = true) }
            val jobSeries = async { sincronizarSeries(dns, user, pass, priority = true) }

            val sucessoInicial = jobFilmes.await() && jobSeries.await()

            // Após liberar a Home com os primeiros dados, dispara o download do resto em background
            if (sucessoInicial) {
                launch { atualizarTudo(dns, user, pass) }
            }

            return@withContext sucessoInicial
        } catch (e: Exception) {
            Log.e("IPTV_REPO", "Erro na sincronização: ${e.message}")
            false
        }
    }

    private suspend fun atualizarTudo(dns: String, user: String, pass: String) {
        sincronizarFilmes(dns, user, pass, priority = false)
        sincronizarSeries(dns, user, pass, priority = false)
    }

    private suspend fun sincronizarFilmes(dns: String, user: String, pass: String, priority: Boolean): Boolean {
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

                // Se for prioridade, pegamos apenas os primeiros 50 para abrir a Home JÁ CHEIA
                val listToProcess = if (priority) movies.take(50) else movies

                val entities = listToProcess.map { 
                    MovieEntity(
                        streamId = it.streamId,
                        name = it.name,
                        containerExtension = it.containerExtension,
                        categoryId = it.categoryId ?: "0",
                        streamIcon = it.streamIcon,
                        rating = it.rating
                    )
                }

                if (!priority) movieDao.clearAll() 
                movieDao.insertAll(entities)
                Log.d("IPTV_REPO", "Filmes (${if(priority) "Priority" else "Full"}): ${entities.size}")
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun sincronizarSeries(dns: String, user: String, pass: String, priority: Boolean): Boolean {
        return try {
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
                val listType = object : TypeToken<List<IptvSeries>>() {}.type
                val series: List<IptvSeries> = gson.fromJson(reader, listType)

                val listToProcess = if (priority) series.take(50) else series

                val entities = listToProcess.map { 
                    SeriesEntity(
                        seriesId = it.seriesId.toInt(),
                        name = it.name,
                        seriesIdString = it.seriesId,
                        cover = it.cover ?: it.lastModified,
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

                if (!priority) seriesDao.clearAll()
                seriesDao.insertAll(entities)
                Log.d("IPTV_REPO", "Séries (${if(priority) "Priority" else "Full"}): ${entities.size}")
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
