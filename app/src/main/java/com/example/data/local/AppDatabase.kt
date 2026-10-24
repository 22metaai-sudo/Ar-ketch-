package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DrawingProject
import com.example.data.model.TimelapseSession

@Database(entities = [DrawingProject::class, TimelapseSession::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drawingDao(): DrawingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "omni_ar_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
