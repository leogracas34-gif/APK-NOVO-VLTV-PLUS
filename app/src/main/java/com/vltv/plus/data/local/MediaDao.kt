package com.vltv.plus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mediaList: List<MediaEntity>)

    @Query("SELECT * FROM media_content WHERE streamType = :type ORDER BY name ASC")
    suspend fun getContentByType(type: String): List<MediaEntity>

    @Query("SELECT * FROM media_content WHERE categoryId = :catId AND streamType = :type")
    suspend fun getContentByCategory(catId: String, type: String): List<MediaEntity>

    @Query("DELETE FROM media_content")
    suspend fun clearDatabase()
}
