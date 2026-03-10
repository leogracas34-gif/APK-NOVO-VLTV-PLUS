package com.vltvplus.iptv

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.zip.GZIPInputStream

class IptvRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val streamDao = db.streamDao()
    private val client = OkHttpClient()
    private val gson = Gson()

    // FUNÇÃO PRINCIPAL: Sincroniza Canais, Filmes e Séries usando o DNS escolhido no Login
    suspend fun sincronizarConteudoTotal(dns: String, user: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Se o banco já tem VODs salvos, libera a entrada imediata para o app ser rápido
            val temDados = streamDao.getVodCount() > 0
            
            if (temDados) {
                // Atualiza o resto em background sem travar o usuário
                launch { atualizarTudo(dns, user, pass) }
                return@withContext true
            }

            // Carga prioritária simultânea (baixa os 100 primeiros de cada seção para popular a Home rápido)
            val jobs = listOf(
                async { sincronizarLive(dns, user, pass, priority = true) },
                async { sincronizarFilmes(dns, user, pass, priority = true) },
                async { sincronizarSeries(dns, user, pass, priority = true) }
            )

            val sucessoInicial = jobs.awaitAll().all { it }

            if (sucessoInicial) {
                // Após o sucesso inicial, continua baixando o acervo completo em background
                launch { atualizarTudo(dns, user, pass) }
            }

            return@withContext sucessoInicial
        } catch (e: Exception) {
            Log.e("IPTV_REPO", "Erro na sincronização: ${e.message}")
            false
        }
    }

    private suspend fun atualizarTudo(dns: String, user: String, pass: String) {
        sincronizarLive(dns, user, pass, priority = false)
        sincronizarFilmes(dns, user, pass, priority = false)
        sincronizarSeries(dns, user, pass, priority = false)
    }

    private suspend fun sincronizarLive(dns: String, user: String, pass: String, priority: Boolean): Boolean {
        return try {
            val url = "$dns/player_api.php?username=$user&password=$pass&action=get_live_streams"
            val data = fetchData<List<IptvLive>>(url) ?: return false
            
            val list = if (priority) data.take(100) else data
            val entities = list.map { 
                LiveStreamEntity(
                    stream_id = it.stream_id.toInt(),
                    name = it.name,
                    stream_icon = it.stream_icon,
                    epg_channel_id = it.epg_channel_id,
                    category_id = it.category_id ?: "0"
                )
            }
            if (!priority) streamDao.clearLive() // Limpa apenas na carga completa para atualizar
            streamDao.insertLiveStreams(entities)
            true
        } catch (e: Exception) { false }
    }

    private suspend fun sincronizarFilmes(dns: String, user: String, pass: String, priority: Boolean): Boolean {
        return try {
            val url = "$dns/player_api.php?username=$user&password=$pass&action=get_vod_streams"
            val data = fetchData<List<IptvMovie>>(url) ?: return false

            val list = if (priority) data.take(100) else data
            val entities = list.map { 
                VodEntity(
                    stream_id = it.stream_id.toInt(),
                    name = it.name,
                    title = it.name,
                    stream_icon = it.stream_icon,
                    container_extension = it.container_extension,
                    rating = it.rating,
                    category_id = it.category_id ?: "0",
                    added = it.added?.toLongOrNull() ?: 0L
                )
            }
            streamDao.insertVodStreams(entities)
            true
        } catch (e: Exception) { false }
    }

    private suspend fun sincronizarSeries(dns: String, user: String, pass: String, priority: Boolean): Boolean {
        return try {
            val url = "$dns/player_api.php?username=$user&password=$pass&action=get_series"
            val data = fetchData<List<IptvSeries>>(url) ?: return false

            val list = if (priority) data.take(100) else data
            val entities = list.map { 
                SeriesEntity(
                    series_id = it.series_id.toInt(),
                    name = it.name,
                    cover = it.cover,
                    rating = it.rating,
                    category_id = it.category_id ?: "0",
                    last_modified = it.last_modified?.toLongOrNull() ?: 0L
                )
            }
            streamDao.insertSeriesStreams(entities)
            true
        } catch (e: Exception) { false }
    }

    // Função utilitária para buscar dados com suporte a compressão GZIP (essencial para listas grandes)
    private suspend inline fun <reified T> fetchData(url: String): T? {
        return try {
            val request = Request.Builder().url(url).addHeader("Accept-Encoding", "gzip").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val source = response.body?.source()
                val inputStream = if (response.header("Content-Encoding") == "gzip") GZIPInputStream(source?.inputStream()) else source?.inputStream()
                val reader = inputStream?.bufferedReader()
                val type = object : TypeToken<T>() {}.type
                gson.fromJson(reader, type)
            }
        } catch (e: Exception) { null }
    }
}

// Data classes de mapeamento para o GSON (Refletem os campos exatos do seu JSON)
data class IptvLive(val stream_id: String, val name: String, val stream_icon: String?, val epg_channel_id: String?, val category_id: String?)
data class IptvMovie(val stream_id: String, val name: String, val stream_icon: String?, val container_extension: String?, val rating: String?, val category_id: String?, val added: String?)
data class IptvSeries(val stream_id: String, val series_id: String, val name: String, val cover: String?, val rating: String?, val category_id: String?, val last_modified: String?)
