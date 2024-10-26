package com.azhar.absensi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.azhar.absensi.model.User

@Dao
interface UserDao {
    @Insert
    fun insertUser(user: User)

    @Query("SELECT * FROM tbl_users WHERE email = :email AND password = :password AND isDeleted = 0")
    fun getUserByCredentials(email: String, password: String): User?

    @Update
    fun updateUser(user: User)

    @Query("UPDATE tbl_users SET isDeleted = 1 WHERE uid = :uid")
    fun softDeleteUser(uid: Int)

    @Query("SELECT * FROM tbl_users WHERE isDeleted = 0")
    fun getAllUsers(): List<User>

    @Query("SELECT uid FROM tbl_users WHERE email = :email AND isDeleted = 0")
    fun getUserIdByEmail(email: String): Int?
}
