package com.example.runts.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val ALL = arrayOf(object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE training_sheets ADD COLUMN pendingSync INTEGER NOT NULL DEFAULT 0")
        }
    }, object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE workout_executions ADD COLUMN sourceProvider TEXT")
            db.execSQL("ALTER TABLE workout_executions ADD COLUMN sourceActivityId TEXT")
        }
    })
}
