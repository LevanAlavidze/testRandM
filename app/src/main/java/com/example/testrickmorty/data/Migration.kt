package com.example.testrickmorty.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example: Adding a new column to CharacterEntity table
        database.execSQL("ALTER TABLE CharacterEntity ADD COLUMN new_column_name TEXT")
    }
}