package com.vltvplus.iptv

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<MovieEntity>)

    @Query("SELECT * FROM movies_table WHERE categoryId = :catId")
    suspend fun getMoviesByCategory(catId: String): List<MovieEntity>

    @Query("SELECT * FROM movies_table")
    suspend fun getAllMovies(): List<MovieEntity>

    @Query("DELETE FROM movies_table")
    suspend fun clearAll()

    // CORREÇÃO DO ERRO: Função necessária para o IptvRepository verificar se o banco está vazio
    @Query("SELECT COUNT(*) FROM movies_table")
    suspend fun getCount(): Int
}
