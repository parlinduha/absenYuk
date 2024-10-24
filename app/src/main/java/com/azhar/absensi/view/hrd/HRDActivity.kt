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

        // Tambahkan data dummy
        userList.addAll(listOf(
            User(1, "John Doe", "password123", "user"),
            User(2, "Jane Smith", "password456", "user")
        ))
        userAdapter.notifyDataSetChanged()

        // Tambahkan data admin secara hardcode
        CoroutineScope(Dispatchers.IO).launch {
            val adminUser = User(0, "admin", "admin123", "admin")
            db.userDao().insertUser(adminUser)
        }

        // Set listener untuk tombol tambah
        btnAddUser.setOnClickListener {
            showAddDialog()
        }
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_user, null)
        val builder = AlertDialog.Builder(this)
            .setTitle("Tambah Pengguna")
            .setView(dialogView)
            .setPositiveButton("Tambah") { dialog, which ->
                val name = dialogView.etName.text.toString()
                val password = dialogView.etPassword.text.toString()
                if (name.isNotEmpty() && password.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val newUser = User(0, name, password, "user")
                        db.userDao().insertUser(newUser)
                        runOnUiThread {
                            userList.add(newUser)
                            userAdapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    Toast.makeText(this, "Nama dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal") { dialog, which -> dialog.cancel() }
        builder.create().show()
    }

    private fun showEditDialog(user: User) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_user, null)
        dialogView.etName.setText(user.name)
        dialogView.etPassword.setText(user.password)
        val builder = AlertDialog.Builder(this)
            .setTitle("Edit Pengguna")
            .setView(dialogView)
            .setPositiveButton("Simpan") { dialog, which ->
                val name = dialogView.etName.text.toString()
                val password = dialogView.etPassword.text.toString()
                if (name.isNotEmpty() && password.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val updatedUser = User(user.uid, name, password, user.role)
                        db.userDao().insertUser(updatedUser)
                        runOnUiThread {
                            user.name = name
                            user.password = password
                            userAdapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    Toast.makeText(this, "Nama dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal") { dialog, which -> dialog.cancel() }
        builder.create().show()
    }

    private fun showDeleteDialog(user: User) {
        val builder = AlertDialog.Builder(this)
            .setTitle("Hapus Pengguna")
            .setMessage("Yakin ingin menghapus pengguna ${user.name}?")
            .setPositiveButton("Ya") { dialog, which ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.userDao().deleteUser(user.uid)
                    runOnUiThread {
                        userList.remove(user)
                        userAdapter.notifyDataSetChanged()
                    }
                }
            }
            .setNegativeButton("Batal") { dialog, which -> dialog.cancel() }
        builder.create().show()
    }
}
