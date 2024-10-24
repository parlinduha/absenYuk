package com.azhar.absensi.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_users")
data class User(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    var name: String,
    var password: String,
    val role: String
)
