package com.azhar.absensi.view.history

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityHistoryBinding
import com.azhar.absensi.model.ModelDatabase
import com.azhar.absensi.utils.SessionLogin
import com.azhar.absensi.viewmodel.HistoryViewModel

class HistoryActivity : AppCompatActivity(), HistoryAdapter.HistoryAdapterCallback {
    private lateinit var binding: ActivityHistoryBinding
    private var modelDatabaseList: MutableList<ModelDatabase> = ArrayList()
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var session: SessionLogin
    private lateinit var userRole: String
    private lateinit var userEmail: String
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionLogin(applicationContext)
        userRole = session.getUserRole() ?: ""
        userEmail = session.getUserEmail() ?: ""
        Log.d("History Session", userEmail)
        userId = session.getUserUid()

        setInitLayout()
        setViewModel()
    }

    private fun setInitLayout() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.tvNotFound.visibility = View.GONE

        historyAdapter = HistoryAdapter(this, modelDatabaseList, this)
        binding.rvHistory.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
    }

    private fun setViewModel() {
        historyViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
        if (userRole == "admin") {
            historyViewModel.getAllHistory().observe(this) { modelDatabases: List<ModelDatabase> ->
                updateUI(modelDatabases)
            }
        } else {
            historyViewModel.getHistoryByRoleAndUid(userEmail).observe(this) { modelDatabases: List<ModelDatabase> ->
                updateUI(modelDatabases)
            }
        }
    }

    private fun updateUI(modelDatabases: List<ModelDatabase>) {
        if (modelDatabases.isEmpty()) {
            binding.tvNotFound.visibility = View.VISIBLE
            binding.rvHistory.visibility = View.GONE
        } else {
            binding.tvNotFound.visibility = View.GONE
            binding.rvHistory.visibility = View.VISIBLE
        }
        historyAdapter.setDataAdapter(modelDatabases)
    }

    override fun onDelete(modelDatabase: ModelDatabase?) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("Hapus riwayat ini?")
        alertDialogBuilder.setPositiveButton("Ya, Hapus") { dialogInterface, _ ->
            modelDatabase?.uid?.let { uid ->
                historyViewModel.deleteDataById(uid)
                Toast.makeText(
                    this@HistoryActivity,
                    "Yeay! Data yang dipilih sudah dihapus",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        alertDialogBuilder.setNegativeButton("Batal") { dialogInterface: DialogInterface, _ ->
            dialogInterface.cancel()
        }
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