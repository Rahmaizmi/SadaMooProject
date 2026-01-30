package com.example.sadamoo.users.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Date

@Database(
    entities = [Detection::class, ScanHistory::class, Consultation::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DetectionRoomDatabase : RoomDatabase() {

    abstract fun detectionDao(): DetectionDao
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun consultationDao(): ConsultationDao

    companion object {
        @Volatile
        private var INSTANCE: DetectionRoomDatabase? = null
        fun getDatabase(context: Context): DetectionRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DetectionRoomDatabase::class.java,
                    "detection_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
