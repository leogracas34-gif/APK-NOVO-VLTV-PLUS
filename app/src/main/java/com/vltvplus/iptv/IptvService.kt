package com.vltvplus.iptv

import retrofit2.http.GET
import retrofit2.http.Query

interface IptvService {
    
    // Busca a lista completa de Filmes (VOD)
    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_vod_streams"
    ): List<IptvMovie> // Corrigido para usar o modelo que está no Repository

    // Busca as categorias de Filmes
    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_vod_categories"
    ): List<CategoryEntity> // Corrigido para usar a Entity que está no seu Database

    // Busca a lista de Canais ao Vivo
    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_live_streams"
    ): List<IptvLive> // Corrigido de IptvLiveStream para IptvLive (padrão do Repository)

    // Busca as Séries
    @GET("player_api.php")
    suspend fun getSeries(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_series"
    ): List<IptvSeries>
}
