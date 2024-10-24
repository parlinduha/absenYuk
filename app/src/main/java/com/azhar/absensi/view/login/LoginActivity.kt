package com.azhar.absensi.view.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.azhar.absensi.R
import com.azhar.absensi.utils.SessionLogin
import com.azhar.absensi.view.main.MainActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    lateinit var session: SessionLogin
    lateinit var strNama: String
    lateinit var strPassword: String
    var REQ_PERMISSION = 101

    // Hardcoded admin credentials
    private val adminUsername = "admin"
    private val adminPassword = "admin123"


    private val penggunaUsername = "user"
    private val penggunaPassword = "user123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setPermission()
        setInitLayout()
    }

    private fun setPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
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

        btnLogin.setOnClickListener {
            strNama = inputNama.text.toString()
            strPassword = inputPassword.text.toString()

            if (strNama.isEmpty() || strPassword.isEmpty()) {
                Toast.makeText(this@LoginActivity, "Form tidak boleh kosong!",
                    Toast.LENGTH_SHORT).show()
            } else {
                // Check if the login is for admin
                if (strNama == adminUsername && strPassword == adminPassword) {
                    // Admin login success
                    Toast.makeText(this@LoginActivity, "Login sebagai Admin", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    session.createLoginSession(strNama, "admin", 1)  // Save role as admin
                }
                // If not admin, check if the user exists
                else if (strNama == penggunaUsername &&  strPassword == penggunaPassword) {
//                else if (session.checkUserExists(strNama, strPassword)) {
                    // User login success
                    Toast.makeText(this@LoginActivity, "Login sebagai Pengguna", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    session.createLoginSession(strNama, "user", 2)  // Save role as user
                } else {
                    Toast.makeText(this@LoginActivity, "Nama atau Password salah!",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                val intent = intent
                finish()
                startActivity(intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
