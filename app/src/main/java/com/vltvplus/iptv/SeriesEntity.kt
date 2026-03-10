package com.vltvplus.iptv

import androidx.room.Entity
import androidx.room.PrimaryKey

// O nome da tabela DEVE ser "series_streams" para bater com o seu StreamDao
@Entity(tableName = "series_streams")
data class SeriesEntity(
    @PrimaryKey 
    val series_id: Int,
    val name: String,
    val cover: String?,
    val rating: String?,
    val category_id: String,
    val last_modified: Long,
    val logo_url: String? = null
)
