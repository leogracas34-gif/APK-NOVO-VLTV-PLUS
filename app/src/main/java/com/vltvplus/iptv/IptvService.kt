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
    ): List<IptvMovie>

    // Busca as categorias de Filmes
    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_vod_categories"
    ): List<IptvCategory>

    // Busca a lista de Canais ao Vivo
    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_live_streams"
    ): List<IptvLiveStream>
}
