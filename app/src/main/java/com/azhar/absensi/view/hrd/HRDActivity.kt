package com.azhar.absensi.view.hrd

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.azhar.absensi.R
import com.azhar.absensi.database.AppDatabase
import com.azhar.absensi.databinding.ActivityHrdBinding
import com.azhar.absensi.databinding.DialogUserBinding
import com.azhar.absensi.model.User
import com.azhar.absensi.utils.UserAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HRDActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHrdBinding
    private lateinit var userAdapter: UserAdapter
    private var userList = mutableListOf<User>()
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHrdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi database
        db = AppDatabase.getDatabase(this)

        // Inisialisasi RecyclerView
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(userList) { user, action ->
            when (action) {
                "edit" -> showEditDialog(user)
                "delete" -> showDeleteDialog(user)
            }
        }
        binding.rvUsers.adapter = userAdapter

        // Muat data pengguna dari database
        loadUsers()

        // Set listener untuk tombol tambah
        binding.btnAddUser.setOnClickListener {
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
        val dialogBinding = DialogUserBinding.inflate(LayoutInflater.from(this))
        val builder = AlertDialog.Builder(this)
            .setTitle("Tambah Pengguna")
            .setView(dialogBinding.root)
            .setPositiveButton("Tambah") { dialog, which ->
                val email = dialogBinding.etName.text.toString()
                val password = dialogBinding.etPassword.text.toString()
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    if (isValidEmail(email)) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val newUser = User(0, email, password, "user")
                            db.userDao().insertUser(newUser)
                            runOnUiThread {
                                loadUsers()
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
        val dialogBinding = DialogUserBinding.inflate(LayoutInflater.from(this))
        dialogBinding.etName.setText(user.email)
        dialogBinding.etPassword.setText(user.password)
        val builder = AlertDialog.Builder(this)
            .setTitle("Edit Pengguna")
            .setView(dialogBinding.root)
            .setPositiveButton("Simpan") { dialog, which ->
                val email = dialogBinding.etName.text.toString()
                val password = dialogBinding.etPassword.text.toString()
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    if (isValidEmail(email)) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val updatedUser = User(user.uid, email, password, user.role)
                            db.userDao().insertUser(updatedUser)
                            runOnUiThread {
                                loadUsers()
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
                        loadUsers()
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