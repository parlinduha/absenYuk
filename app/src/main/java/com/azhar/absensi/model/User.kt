package com.azhar.absensi.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_users")
data class User(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    var email: String,
    var password: String,
    val role: String,
    val name: String,
    var isDeleted: Boolean = false // Untuk soft delete
)
