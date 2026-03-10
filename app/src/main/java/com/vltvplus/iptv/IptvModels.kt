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

// Modelo para as Séries (Elite, Novidades, etc.) do servidor IPTV
data class IptvSeries(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("series_id") val seriesId: String,
    @SerializedName("cover") val cover: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("cast") val cast: String?,
    @SerializedName("director") val director: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("releaseDate") val releaseDate: String?,
    @SerializedName("last_modified") val lastModified: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("category_id") val categoryId: String?
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
