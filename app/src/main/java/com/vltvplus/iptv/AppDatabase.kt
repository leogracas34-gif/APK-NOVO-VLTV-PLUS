package com.vltvplus.iptv

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Adicionado SeriesEntity à lista de entidades e incrementada a versão para refletir a mudança
@Database(entities = [MovieEntity::class, SeriesEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun movieDao(): MovieDao
    // Novo acesso para o banco de dados de séries
    abstract fun seriesDao(): SeriesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vltv_plus_db"
                )
                // O fallbackToDestructiveMigration limpa o banco antigo e cria o novo com as séries
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
