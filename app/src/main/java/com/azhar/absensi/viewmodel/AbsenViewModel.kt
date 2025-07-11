package com.azhar.absensi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.azhar.absensi.database.DatabaseClient
import com.azhar.absensi.database.dao.DatabaseDao
import com.azhar.absensi.model.ModelDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.kotlin.addTo

class AbsenViewModel(application: Application) : AndroidViewModel(application) {

    private val databaseDao: DatabaseDao? = DatabaseClient.getInstance(application)?.appDatabase?.databaseDao()
    private val compositeDisposable = CompositeDisposable()

    private val _insertResult = MutableLiveData<Boolean>()
    val insertResult: LiveData<Boolean> = _insertResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun addDataAbsen(
        foto: String,
        nama: String,
        tanggal: String,
        lokasi: String,
        keterangan: String,
        email: String
    ) {
        if (databaseDao == null) {
            _errorMessage.postValue("Database not initialized")
            return
        }

        val modelDatabase = ModelDatabase().apply {
            fotoSelfie = foto
            this.nama = nama
            this.tanggal = tanggal
            this.lokasi = lokasi
            this.keterangan = keterangan
            this.email = email
        }

        Completable.fromAction {
            databaseDao.insertData(modelDatabase)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _insertResult.postValue(true)
            }, { error ->
                _errorMessage.postValue("Failed to save data: ${error.localizedMessage}")
                _insertResult.postValue(false)
            })
            .addTo(compositeDisposable)
    }



    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}