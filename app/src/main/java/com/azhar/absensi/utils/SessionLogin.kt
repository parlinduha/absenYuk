package com.azhar.absensi.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.azhar.absensi.view.login.LoginActivity

class SessionLogin(var context: Context) {
    var pref: SharedPreferences
    var editor: SharedPreferences.Editor
    var PRIVATE_MODE = 0


    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)


    fun createLoginSession(nama: String, role: String, uid: Int) {
        Log.d("LoginSession", "Creating login session")
        Log.d("LoginSession", "Nama: $nama")
        Log.d("LoginSession", "Role: $role")
        Log.d("LoginSession", "UID: $uid")

        editor.putBoolean(IS_LOGIN, true)
        editor.putString(KEY_NAMA, nama)
        editor.putString(KEY_ROLE, role)
        editor.putInt(KEY_UID, uid)
        editor.commit()
    }

    fun getUserUid(): Int {
        return sharedPreferences.getInt(KEY_UID, 0) // Assuming you store user UID as an integer
    }
    fun getUserRole(): String? {
        return sharedPreferences.getString(KEY_ROLE, null)
    }
    fun checkUserExists(nama: String, password: String): Boolean {
        // Replace this logic with real user validation
        // For now, assume a user is stored in SharedPreferences
        val storedNama = pref.getString(KEY_NAMA, null)
        val storedPassword = pref.getString(KEY_PASSWORD, null)
        return storedNama == nama && storedPassword == password
    }

    fun checkLogin() {
        if (!isLoggedIn()) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    fun logoutUser() {
        editor.clear()
        editor.commit()
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun isLoggedIn(): Boolean = pref.getBoolean(IS_LOGIN, false)

    companion object {
        private const val PREF_NAME = "AbsensiPref"
        private const val IS_LOGIN = "IsLoggedIn"
        const val KEY_NAMA = "NAMA"
        const val KEY_PASSWORD = "PASSWORD"  // For storing the user password
        const val KEY_ROLE = "ROLE"  // For storing the role (admin/user)
        const val KEY_UID = "UID"
    }

    init {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }
}
