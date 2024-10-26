package com.azhar.absensi.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.azhar.absensi.model.ModelDatabase

@Dao
interface DatabaseDao {

    @Query("SELECT * FROM tbl_absensi")
    fun getAllHistory(): LiveData<List<ModelDatabase>>

    @Query("SELECT * FROM tbl_absensi WHERE email = :email")
    fun getHistory(email: String): LiveData<List<ModelDatabase>>

    @Query("SELECT * FROM tbl_absensi WHERE user_id = :userId")
    fun getHistoryByUid(userId: Int): LiveData<List<ModelDatabase>>

    @Query("SELECT a.* FROM tbl_absensi a JOIN tbl_users u ON a.user_id = u.uid WHERE a.user_id = :userId AND u.role = :role")
    fun getHistoryByRoleAndUid(userId: Int, role: String): LiveData<List<ModelDatabase>>

    @Insert
    fun insertData(modelDatabases: ModelDatabase)

    @Query("DELETE FROM tbl_absensi WHERE uid = :uid")
    fun deleteHistoryById(uid: Int)

    @Query("DELETE FROM tbl_absensi")
    fun deleteAllHistory()
}
