package com.azhar.absensi.view.history

import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.azhar.absensi.R
import com.azhar.absensi.model.ModelDatabase
import com.azhar.absensi.utils.SessionLogin
import com.azhar.absensi.viewmodel.HistoryViewModel
import kotlinx.android.synthetic.main.activity_history.*

class HistoryActivity : AppCompatActivity(), HistoryAdapter.HistoryAdapterCallback {
    private var modelDatabaseList: MutableList<ModelDatabase> = ArrayList()
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var session: SessionLogin
    private lateinit var userRole: String
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        session = SessionLogin(applicationContext) // Inisialisasi SessionLogin
        userRole = session.getUserRole() ?: ""
        userId = session.getUserUid()

        setInitLayout()
        setViewModel()
    }

    private fun setInitLayout() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        tvNotFound.visibility = View.GONE

        historyAdapter = HistoryAdapter(this, modelDatabaseList, this)
        rvHistory.setHasFixedSize(true)
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = historyAdapter
    }

    private fun setViewModel() {
        historyViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
        if (userRole == "admin") {
            historyViewModel.getAllHistory().observe(this) { modelDatabases: List<ModelDatabase> ->
                updateUI(modelDatabases)
            }
        } else {
            historyViewModel.getHistoryByUid(userId).observe(this) { modelDatabases: List<ModelDatabase> ->
                updateUI(modelDatabases)
            }
        }
    }

    private fun updateUI(modelDatabases: List<ModelDatabase>) {
        if (modelDatabases.isEmpty()) {
            tvNotFound.visibility = View.VISIBLE
            rvHistory.visibility = View.GONE
        } else {
            tvNotFound.visibility = View.GONE
            rvHistory.visibility = View.VISIBLE
        }
        historyAdapter.setDataAdapter(modelDatabases)
    }

    override fun onDelete(modelDatabase: ModelDatabase?) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("Hapus riwayat ini?")
        alertDialogBuilder.setPositiveButton("Ya, Hapus") { dialogInterface, i ->
            val uid = modelDatabase!!.uid
            historyViewModel.deleteDataById(uid)
            Toast.makeText(this@HistoryActivity, "Yeay! Data yang dipilih sudah dihapus", Toast.LENGTH_SHORT).show()
        }
        alertDialogBuilder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int -> dialogInterface.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
