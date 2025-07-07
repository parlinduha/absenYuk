package com.azhar.absensi.view.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityMainBinding
import com.azhar.absensi.utils.SessionLogin
import com.azhar.absensi.view.absen.AbsenActivity
import com.azhar.absensi.view.history.HistoryActivity
import com.azhar.absensi.view.hrd.HRDActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionLogin
    private lateinit var userRole: String
    private lateinit var currentUserId: String
    private lateinit var strTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi session
        session = SessionLogin(this)
        session.checkLogin()
        currentUserId = session.getUserUid().toString()

        // Ambil role dari session
        userRole = session.getUserRole() ?: "pengguna"

        // Panggil fungsi untuk mengatur tampilan sesuai role
        setInitLayout(userRole)
    }

    private fun setInitLayout(role: String) {
        if (role == "admin") {
            // Hanya admin yang bisa melihat history, sembunyikan lainnya
            binding.cvAbsenMasuk.visibility = android.view.View.GONE
            binding.cvAbsenKeluar.visibility = android.view.View.GONE
            binding.cvPerizinan.visibility = android.view.View.GONE
            binding.cvHistory.visibility = android.view.View.VISIBLE
            binding.cvHRD.visibility = android.view.View.VISIBLE
        } else {
            // Pengguna biasa
            binding.cvAbsenMasuk.visibility = android.view.View.VISIBLE
            binding.cvAbsenKeluar.visibility = android.view.View.VISIBLE
            binding.cvPerizinan.visibility = android.view.View.VISIBLE
            binding.cvHistory.visibility = android.view.View.VISIBLE
            binding.cvHRD.visibility = android.view.View.GONE
        }

        // Set listener untuk Absen Masuk
        binding.cvAbsenMasuk.setOnClickListener {
            strTitle = "Absen Masuk"
            navigateToAbsenActivity(strTitle)
        }

        // Set listener untuk Absen Keluar
        binding.cvAbsenKeluar.setOnClickListener {
            strTitle = "Absen Keluar"
            navigateToAbsenActivity(strTitle)
        }

        // Set listener untuk Izin
        binding.cvPerizinan.setOnClickListener {
            strTitle = "Izin"
            navigateToAbsenActivity(strTitle)
        }

        // Set listener untuk History
        binding.cvHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Set listener untuk HRD
        binding.cvHRD.setOnClickListener {
            startActivity(Intent(this, HRDActivity::class.java))
        }

        // Set listener untuk Logout
        binding.imageLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun navigateToAbsenActivity(title: String) {
        val intent = Intent(this, AbsenActivity::class.java).apply {
            putExtra(AbsenActivity.DATA_TITLE, title)
        }
        startActivity(intent)
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("Yakin Anda ingin Logout?")
            setCancelable(true)
            setNegativeButton("Batal") { dialog, _ -> dialog.cancel() }
            setPositiveButton("Ya") { _, _ ->
                session.logoutUser()
                finishAffinity()
            }
            create().show()
        }
    }
}