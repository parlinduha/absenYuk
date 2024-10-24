package com.azhar.absensi.view.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.azhar.absensi.R
import com.azhar.absensi.utils.SessionLogin
import com.azhar.absensi.view.absen.AbsenActivity
import com.azhar.absensi.view.history.HistoryActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var strTitle: String
    lateinit var session: SessionLogin
    lateinit var userRole: String
    lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi session
        session = SessionLogin(this)
        session.checkLogin() // Pastikan pengguna sudah login
        currentUserId = session.getUserUid().toString()


        // Ambil role dari session
        userRole = session.getUserRole() ?: "pengguna" // Default adalah 'pengguna'

        // Panggil fungsi untuk mengatur tampilan sesuai role
        setInitLayout(userRole)
    }

    private fun setInitLayout(role: String) {
        if (role == "admin") {
            // Hanya admin yang bisa melihat history, sembunyikan lainnya
            cvAbsenMasuk.visibility = android.view.View.GONE
            cvAbsenKeluar.visibility = android.view.View.GONE
            cvPerizinan.visibility = android.view.View.GONE
            cvHistory.visibility = android.view.View.VISIBLE // Admin dapat melihat history
        } else {
            // Pengguna biasa dapat melihat absen dan perizinan, tapi tidak history
            cvAbsenMasuk.visibility = android.view.View.VISIBLE
            cvAbsenKeluar.visibility = android.view.View.VISIBLE
            cvPerizinan.visibility = android.view.View.VISIBLE
            cvHistory.visibility = android.view.View.VISIBLE // Pengguna  bisa melihat history sesuai id nya
        }

        // Set listener untuk Absen Masuk
        cvAbsenMasuk.setOnClickListener {
            strTitle = "Absen Masuk"
            val intent = Intent(this@MainActivity, AbsenActivity::class.java)
            intent.putExtra(AbsenActivity.DATA_TITLE, strTitle)
            startActivity(intent)
        }

        // Set listener untuk Absen Keluar
        cvAbsenKeluar.setOnClickListener {
            strTitle = "Absen Keluar"
            val intent = Intent(this@MainActivity, AbsenActivity::class.java)
            intent.putExtra(AbsenActivity.DATA_TITLE, strTitle)
            startActivity(intent)
        }

        // Set listener untuk Izin
        cvPerizinan.setOnClickListener {
            strTitle = "Izin"
            val intent = Intent(this@MainActivity, AbsenActivity::class.java)
            intent.putExtra(AbsenActivity.DATA_TITLE, strTitle)
            startActivity(intent)
        }

        // Set listener untuk History (hanya admin)
        cvHistory.setOnClickListener {
            val intent = Intent(this@MainActivity, HistoryActivity::class.java)
            startActivity(intent)
        }

        // Set listener untuk Logout
        imageLogout.setOnClickListener {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setMessage("Yakin Anda ingin Logout?")
            builder.setCancelable(true)
            builder.setNegativeButton("Batal") { dialog, _ -> dialog.cancel() }
            builder.setPositiveButton("Ya") { _, _ ->
                session.logoutUser()
                finishAffinity() // Menutup semua aktivitas dan kembali ke layar login
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }
}
