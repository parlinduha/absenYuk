package com.azhar.absensi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.azhar.absensi.model.User

@Dao
interface UserDao {
    @Insert
    fun insertUser(user: User)

    @Query("SELECT * FROM tbl_users WHERE name = :name AND password = :password")
    fun getUserByCredentials(name: String, password: String): User?

    @Query("DELETE FROM tbl_users WHERE uid = :uid")
    fun deleteUser(uid: Int)
}
