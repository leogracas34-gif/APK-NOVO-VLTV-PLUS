package com.vltvplus.iptv

import androidx.room.Entity
import androidx.room.PrimaryKey

// Atualizado para "series_streams" para bater com o StreamDao e IptvRepository
@Entity(tableName = "series_streams")
data class SeriesEntity(
    @PrimaryKey 
    val series_id: Int, // Alterado para snake_case para bater com o padrão da sua Database V6
    val name: String,
    val cover: String?,
    val rating: String?,
    val category_id: String, // Alterado para snake_case
    val last_modified: Long, // Alterado para Long conforme o erro de Cursor do Room
    val logo_url: String? = null
)
