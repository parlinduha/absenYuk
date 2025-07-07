package com.azhar.absensi.view.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.azhar.absensi.R
import com.azhar.absensi.database.AppDatabase
import com.azhar.absensi.databinding.ActivityLoginBinding
import com.azhar.absensi.utils.SessionLogin
import com.azhar.absensi.view.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionLogin
    private var REQ_PERMISSION = 101
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi database
        db = AppDatabase.getDatabase(this)

        setPermission()
        setInitLayout()
    }

    private fun setPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQ_PERMISSION
            )
        }
    }

    private fun setInitLayout() {
        session = SessionLogin(applicationContext)

        if (session.isLoggedIn()) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val strEmail = binding.inputNama.text.toString()
            val strPassword = binding.inputPassword.text.toString()

            if (strEmail.isEmpty() || strPassword.isEmpty()) {
                Toast.makeText(
                    this@LoginActivity,
                    "Form tidak boleh kosong!",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!isValidEmail(strEmail)) {
                Toast.makeText(
                    this@LoginActivity,
                    "Email tidak valid!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val user = db.userDao().getUserByCredentials(strEmail, strPassword)
                    runOnUiThread {
                        if (user != null) {
                            // User login success
                            Toast.makeText(
                                this@LoginActivity,
                                "Login sebagai ${user.role}",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            session.createLoginSession(user.email, user.role, user.uid)
                        } else if (strEmail == "admin@gmail.com" && strPassword == "admin123") {
                            // Admin login success
                            Toast.makeText(
                                this@LoginActivity,
                                "Login sebagai admin",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            session.createLoginSession("admin", "admin", 0)
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Email atau Password salah!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = intent
            finish()
            startActivity(intent)
        }
    }
}