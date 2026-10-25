package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [VocabEntity::class, ReviewLogEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vocabDao(): VocabDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add FSRS columns
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN fsrsStability REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN fsrsDifficulty REAL NOT NULL DEFAULT 5.0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN fsrsState INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN fsrsLapses INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN lastRating INTEGER NOT NULL DEFAULT 0")

                // Add 7 Skill Mastery columns
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN recognitionAttempts INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN recognitionSuccesses INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN recallAttempts INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN recallSuccesses INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN spellingAttempts INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN spellingSuccesses INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN contextAttempts INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN contextSuccesses INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN synonymAttempts INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN synonymSuccesses INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN productionAttempts INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN productionSuccesses INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN listeningAttempts INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN listeningSuccesses INTEGER NOT NULL DEFAULT 0")

                // Add Rich Linguistic Profile columns
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN collocations TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN wordFamily TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN grammarPatterns TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN commonMistakes TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN registerFormality TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE vocab_entries ADD COLUMN antonyms TEXT NOT NULL DEFAULT '[]'")

                // Create review_logs table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS review_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entryNumber INTEGER NOT NULL,
                        rating INTEGER NOT NULL,
                        reviewTimestamp INTEGER NOT NULL,
                        scheduledDays INTEGER NOT NULL,
                        stability REAL NOT NULL,
                        difficulty REAL NOT NULL,
                        channel TEXT NOT NULL DEFAULT 'FSRS'
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_review_logs_entryNumber ON review_logs(entryNumber)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_review_logs_reviewTimestamp ON review_logs(reviewTimestamp)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "toefl_1212_v2.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
