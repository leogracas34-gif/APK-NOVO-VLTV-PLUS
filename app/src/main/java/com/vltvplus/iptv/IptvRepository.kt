package com.vltvplus.iptv

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*

class IptvRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val streamDao = db.streamDao()

    // FUNÇÃO PRINCIPAL: Ativa a conexão usando o RetrofitClient e sincroniza tudo
    suspend fun sincronizarConteudoTotal(dns: String, user: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Inicializa a API com o DNS digitado pelo usuário
            val api = RetrofitClient.getClient(dns)
            
            // Se o banco já tem dados, entra no app e atualiza em segundo plano
            val temDados = streamDao.getVodCount() > 0
            
            if (temDados) {
                launch { atualizarTudo(api, user, pass) }
                return@withContext true
            }

            // Carga prioritária simultânea (usando as rotas reais da API)
            val jobs = listOf(
                async { sincronizarLive(api, user, pass, priority = true) },
                async { sincronizarFilmes(api, user, pass, priority = true) },
                async { sincronizarSeries(api, user, pass, priority = true) }
            )

            val sucessoInicial = jobs.awaitAll().all { it }

            if (sucessoInicial) {
                launch { atualizarTudo(api, user, pass) }
            }

            return@withContext sucessoInicial
        } catch (e: Exception) {
            Log.e("IPTV_REPO", "Erro na sincronização: ${e.message}")
            false
        }
    }

    private suspend fun atualizarTudo(api: IptvService, user: String, pass: String) {
        sincronizarLive(api, user, pass, priority = false)
        sincronizarFilmes(api, user, pass, priority = false)
        sincronizarSeries(api, user, pass, priority = false)
    }

    private suspend fun sincronizarLive(api: IptvService, user: String, pass: String, priority: Boolean): Boolean {
        return try {
            val data = api.getLiveStreams(user, pass)
            
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
            if (!priority) streamDao.clearLive()
            streamDao.insertLiveStreams(entities)
            true
        } catch (e: Exception) { false }
    }

    private suspend fun sincronizarFilmes(api: IptvService, user: String, pass: String, priority: Boolean): Boolean {
        return try {
            val data = api.getVodStreams(user, pass)

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

    private suspend fun sincronizarSeries(api: IptvService, user: String, pass: String, priority: Boolean): Boolean {
        return try {
            val data = api.getSeries(user, pass)

            val list = if (priority) data.take(100) else data
            val entities = list.map { 
                SeriesEntity(
                    series_id = it.series_id?.toInt() ?: it.series_id_alt?.toInt() ?: 0,
                    name = it.name ?: "",
                    cover = it.cover,
                    rating = it.rating,
                    category_id = it.category_id ?: "0",
                    last_modified = it.last_modified?.toLongOrNull() ?: 0L,
                    logo_url = it.cover
                )
            }
            streamDao.insertSeriesStreams(entities)
            true
        } catch (e: Exception) { false }
    }
}

// ==========================================
// MODELOS DE DADOS PARA A API
// ==========================================

data class IptvLive(
    val stream_id: String, 
    val name: String, 
    val stream_icon: String?, 
    val epg_channel_id: String?, 
    val category_id: String?
)

data class IptvMovie(
    val stream_id: String, 
    val name: String, 
    val stream_icon: String?, 
    val container_extension: String?, 
    val rating: String?, 
    val category_id: String?, 
    val added: String?
)

data class IptvSeries(
    val series_id: String?, 
    @com.google.gson.annotations.SerializedName("series_id") val series_id_alt: String?,
    val name: String?, 
    val cover: String?, 
    val rating: String?, 
    val category_id: String?, 
    val last_modified: String?
)
