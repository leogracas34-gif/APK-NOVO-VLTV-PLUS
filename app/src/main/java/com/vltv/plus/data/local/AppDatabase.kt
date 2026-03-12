package com.vltv.plus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.JournalMode

@Database(
    entities = [MediaEntity::class, MediaContentFts::class], 
    version = 1, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vltv_plus_ultra_db"
                )
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING) // Alta performance paralela
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
