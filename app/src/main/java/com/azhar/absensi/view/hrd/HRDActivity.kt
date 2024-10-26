package com.azhar.absensi.view.hrd

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.azhar.absensi.R
import com.azhar.absensi.database.AppDatabase
import com.azhar.absensi.model.User
import com.azhar.absensi.utils.UserAdapter
import kotlinx.android.synthetic.main.activity_hrd.*
import kotlinx.android.synthetic.main.dialog_user.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HRDActivity : AppCompatActivity() {

    private lateinit var userAdapter: UserAdapter
    private var userList = mutableListOf<User>()
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hrd)

        // Inisialisasi database
        db = AppDatabase.getDatabase(this)

        // Inisialisasi RecyclerView
        rvUsers.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(userList) { user, action ->
            when (action) {
                "edit" -> showEditDialog(user)
                "delete" -> showDeleteDialog(user)
            }
        }
        rvUsers.adapter = userAdapter

        // Muat data pengguna dari database
        loadUsers()

        // Set listener untuk tombol tambah
        btnAddUser.setOnClickListener {
            showAddDialog()
        }
    }

    private fun loadUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            val users = db.userDao().getAllUsers()
            runOnUiThread {
                userList.clear()
                userList.addAll(users)
                userAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_user, null)
        val builder = AlertDialog.Builder(this)
            .setTitle("Tambah Pengguna")
            .setView(dialogView)
            .setPositiveButton("Tambah") { dialog, which ->
                val email = dialogView.etName.text.toString()
                val password = dialogView.etPassword.text.toString()
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    if (isValidEmail(email)) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val newUser = User(0, email, password, "user")
                            db.userDao().insertUser(newUser)
                            runOnUiThread {
                                loadUsers() // Muat ulang data pengguna setelah menambahkan pengguna baru
                            }
                        }
                    } else {
                        Toast.makeText(this, "Email tidak valid", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal") { dialog, which -> dialog.cancel() }
        builder.create().show()
    }

    private fun showEditDialog(user: User) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_user, null)
        dialogView.etName.setText(user.email)
        dialogView.etPassword.setText(user.password)
        val builder = AlertDialog.Builder(this)
            .setTitle("Edit Pengguna")
            .setView(dialogView)
            .setPositiveButton("Simpan") { dialog, which ->
                val email = dialogView.etName.text.toString()
                val password = dialogView.etPassword.text.toString()
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    if (isValidEmail(email)) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val updatedUser = User(user.uid, email, password, user.role)
                            db.userDao().insertUser(updatedUser)
                            runOnUiThread {
                                loadUsers() // Muat ulang data pengguna setelah mengedit pengguna
                            }
                        }
                    } else {
                        Toast.makeText(this, "Email tidak valid", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal") { dialog, which -> dialog.cancel() }
        builder.create().show()
    }

    private fun showDeleteDialog(user: User) {
        val builder = AlertDialog.Builder(this)
            .setTitle("Hapus Pengguna")
            .setMessage("Yakin ingin menghapus pengguna ${user.email}?")
            .setPositiveButton("Ya") { dialog, which ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.userDao().softDeleteUser(user.uid)
                    runOnUiThread {
                        loadUsers() // Muat ulang data pengguna setelah menghapus pengguna
                    }
                }
            }
            .setNegativeButton("Batal") { dialog, which -> dialog.cancel() }
        builder.create().show()
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }
}
