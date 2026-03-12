package com.vltv.plus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface MediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mediaList: List<MediaEntity>)

    // Busca ultra-veloz usando FTS4 (Full Text Search)
    @Query("""
        SELECT * FROM media_content 
        JOIN media_content_fts ON media_content.name = media_content_fts.name 
        WHERE media_content_fts MATCH :query
    """)
    suspend fun searchByName(query: String): List<MediaEntity>

    @Query("SELECT * FROM media_content WHERE streamType = :type ORDER BY lastUpdated DESC")
    suspend fun getContentByType(type: String): List<MediaEntity>

    @Query("SELECT * FROM media_content WHERE categoryId = :catId AND streamType = :type")
    suspend fun getContentByCategory(catId: String, type: String): List<MediaEntity>

    @Query("UPDATE media_content SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFav: Boolean)

    @Transaction
    @Query("DELETE FROM media_content")
    suspend fun clearAll()
}
