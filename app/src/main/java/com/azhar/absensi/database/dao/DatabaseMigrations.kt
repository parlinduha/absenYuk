package com.azhar.absensi.database.dao

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS tbl_users (uid INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, password TEXT, role TEXT)")
    }
}