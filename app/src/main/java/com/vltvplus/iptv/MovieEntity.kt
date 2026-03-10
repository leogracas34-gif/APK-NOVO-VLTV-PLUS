package com.vltvplus.iptv

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies_table")
data class MovieEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val streamId: String,
    val name: String,
    val containerExtension: String?,
    val categoryId: String,
    val streamIcon: String?,
    val rating: String?,
    val logoTmdb: String? = null // Espaço reservado para o logo que buscamos
)
