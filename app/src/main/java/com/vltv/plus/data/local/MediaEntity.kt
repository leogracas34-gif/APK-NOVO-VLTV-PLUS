package com.vltv.plus.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

// Tabela principal com índices para busca ultra-rápida por categoria e tipo
@Entity(
    tableName = "media_content",
    indices = [
        Index(value = ["streamType"]),
        Index(value = ["categoryId"]),
        Index(value = ["name"])
    ]
)
data class MediaEntity(
    @PrimaryKey 
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "categoryId") val categoryId: String,
    @ColumnInfo(name = "streamType") val streamType: String, // live, movie, series
    @ColumnInfo(name = "streamIcon") val streamIcon: String?,
    @ColumnInfo(name = "tmdbLogo") val tmdbLogo: String?,
    @ColumnInfo(name = "rating") val rating: String?,
    @ColumnInfo(name = "addedDate") val addedDate: String?,
    @ColumnInfo(name = "containerExtension") val containerExtension: String?,
    @ColumnInfo(name = "customData") val customData: String?, // Para metadados JSON robustos
    @ColumnInfo(name = "isFavorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "lastUpdated") val lastUpdated: Long = System.currentTimeMillis()
)

// Tabela FTS4 para busca instantânea (Full Text Search) - O segredo da velocidade
@Fts4(contentEntity = MediaEntity::class)
@Entity(tableName = "media_content_fts")
data class MediaContentFts(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "categoryId") val categoryId: String
)
