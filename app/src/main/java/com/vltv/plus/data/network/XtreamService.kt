package com.vltv.plus.data.network

import com.vltv.plus.data.network.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface XtreamService {
    
    @GET("player_api.php")
    suspend fun getProfile(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_profile"
    ): Response<LoginResponse>
    
    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories"
    ): Response<CategoryList>
    
    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_categories"
    ): Response<CategoryList>
    
    @GET("player_api.php")
    suspend fun getSeriesCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_categories"
    ): Response<CategoryList>
}
