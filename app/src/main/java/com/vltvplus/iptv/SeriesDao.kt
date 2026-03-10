package com.vltvplus.iptv

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SeriesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(series: List<SeriesEntity>)

    @Query("SELECT * FROM series WHERE categoryId = :catId")
    suspend fun getSeriesByCategory(catId: String): List<SeriesEntity>

    @Query("SELECT * FROM series")
    suspend fun getAllSeries(): List<SeriesEntity>

    @Query("DELETE FROM series")
    suspend fun clearAll()
    
    @Query("SELECT COUNT(*) FROM series")
    suspend fun getCount(): Int
}
