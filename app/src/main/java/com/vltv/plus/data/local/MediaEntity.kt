package com.vltv.plus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_content")
data class MediaEntity(
    @PrimaryKey val id: Int, // ID que vem do servidor Xtream
    val name: String,
    val categoryId: String,
    val streamType: String, // "live", "movie" ou "series"
    val streamIcon: String?, // Logo original do servidor
    val tmdbLogo: String?,   // Logo em PT-BR vinda do TMDB (Carga instantânea)
    val rating: String?,
    val releaseDate: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)
