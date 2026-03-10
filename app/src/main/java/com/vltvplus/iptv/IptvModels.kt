package com.vltvplus.iptv

import com.google.gson.annotations.SerializedName

// Modelo para os Filmes (VOD) do servidor IPTV
data class IptvMovie(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("stream_id") val streamId: String,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("container_extension") val containerExtension: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("rating") val rating: String?
)

// Modelo para Categorias
data class IptvCategory(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String
)

// Modelo para Canais ao Vivo
data class IptvLiveStream(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("stream_id") val streamId: String,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("category_id") val categoryId: String?
)
