package com.azhar.absensi.database

import androidx.room.Database
import com.azhar.absensi.model.ModelDatabase
import androidx.room.RoomDatabase
import com.azhar.absensi.database.dao.DatabaseDao

/**
 * Created by Ketut Suanta on 19-10-2024
 */

@Database(entities = [ModelDatabase::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun databaseDao(): DatabaseDao?
}
