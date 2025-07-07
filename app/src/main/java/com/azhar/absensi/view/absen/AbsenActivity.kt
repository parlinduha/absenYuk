package com.azhar.absensi.view.absen

import android.Manifest
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Geocoder
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import com.azhar.absensi.BuildConfig
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityAbsenBinding
import com.azhar.absensi.utils.BitmapManager.bitmapToBase64
import com.azhar.absensi.utils.SessionLogin
import com.azhar.absensi.viewmodel.AbsenViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.LocationServices
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AbsenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAbsenBinding
    private var REQ_CAMERA = 101
    private var strCurrentLatitude = 0.0
    private var strCurrentLongitude = 0.0
    private var strFilePath: String = ""
    private var strLatitude = "0"
    private var strLongitude = "0"
    private lateinit var fileDirectoty: File
    private lateinit var imageFilename: File
    private lateinit var exifInterface: ExifInterface
    private lateinit var strBase64Photo: String
    private lateinit var strCurrentLocation: String
    private lateinit var strTitle: String
    private lateinit var strTimeStamp: String
    private lateinit var strImageName: String
    private lateinit var session: SessionLogin
    private lateinit var absenViewModel: AbsenViewModel
    private lateinit var progressDialog: ProgressDialog
    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbsenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionLogin(applicationContext)
        userEmail = session.getUserEmail() ?: ""
        Log.d("History Session", userEmail)

        initViewModel()
        setInitLayout()
        setCurrentLocation()
        setUploadData()
    }

    private fun initViewModel() {
        absenViewModel = ViewModelProvider(this).get(AbsenViewModel::class.java)
    }

    private fun setCurrentLocation() {
        progressDialog.show()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                progressDialog.dismiss()
                if (location != null) {
                    strCurrentLatitude = location.latitude
                    strCurrentLongitude = location.longitude
                    val geocoder = Geocoder(this@AbsenActivity, Locale.getDefault())
                    try {
                        val addressList = geocoder.getFromLocation(
                            strCurrentLatitude,
                            strCurrentLongitude,
                            1
                        )
                        if (addressList != null && addressList.size > 0) {
                            strCurrentLocation = addressList[0].getAddressLine(0)
                            binding.inputLokasi.setText(strCurrentLocation)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@AbsenActivity,
                        "Ups, gagal mendapatkan lokasi. Silahkan periksa GPS atau koneksi internet Anda!",
                        Toast.LENGTH_SHORT
                    ).show()
                    strLatitude = "0"
                    strLongitude = "0"
                }
            }
    }

    private fun setInitLayout() {
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading")
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Sedang memuat...")

        strTitle = intent.extras?.getString(DATA_TITLE).toString()

        if (strTitle != null) {
            binding.tvTitle.text = strTitle
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.inputTanggal.setOnClickListener {
            val tanggalAbsen = Calendar.getInstance()
            val date = OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                tanggalAbsen.set(Calendar.YEAR, year)
                tanggalAbsen.set(Calendar.MONTH, monthOfYear)
                tanggalAbsen.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val strFormatDefault = "dd MMMM yyyy HH:mm"
                val simpleDateFormat = SimpleDateFormat(strFormatDefault, Locale.getDefault())
                binding.inputTanggal.setText(simpleDateFormat.format(tanggalAbsen.time))
            }

            DatePickerDialog(
                this@AbsenActivity, date,
                tanggalAbsen.get(Calendar.YEAR),
                tanggalAbsen.get(Calendar.MONTH),
                tanggalAbsen.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.layoutImage.setOnClickListener {
            Dexter.withContext(this@AbsenActivity)
                .withPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            try {
                                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                                    putExtra("android.intent.extras.CAMERA_FACING", 1)
                                    putExtra(
                                        MediaStore.EXTRA_OUTPUT,
                                        FileProvider.getUriForFile(
                                            this@AbsenActivity,
                                            "${BuildConfig.APPLICATION_ID}.provider",
                                            createImageFile()
                                        )
                                    )
                                }
                                startActivityForResult(cameraIntent, REQ_CAMERA)
                            } catch (ex: IOException) {
                                Toast.makeText(
                                    this@AbsenActivity,
                                    "Ups, gagal membuka kamera",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest>,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()
        }
    }

    private fun setUploadData() {
        binding.btnAbsen.setOnClickListener {
            val strNama = binding.inputNama.text.toString()
            val strTanggal = binding.inputTanggal.text.toString()
            val strKeterangan = binding.inputKeterangan.text.toString()

            if (strFilePath.isEmpty() || strNama.isEmpty() || strCurrentLocation.isEmpty() ||
                strTanggal.isEmpty() || strKeterangan.isEmpty()
            ) {
                Toast.makeText(
                    this@AbsenActivity,
                    "Data tidak boleh ada yang kosong!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                absenViewModel.addDataAbsen(
                    strBase64Photo,
                    strNama,
                    strTanggal,
                    strCurrentLocation,
                    strKeterangan,
                    userEmail
                )
                Toast.makeText(
                    this@AbsenActivity,
                    "Laporan Anda terkirim, tunggu info selanjutnya ya!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        strTimeStamp = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        strImageName = "IMG_"
        fileDirectoty = getExternalFilesDir(Environment.DIRECTORY_DCIM) ?: File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            ""
        )
        imageFilename = File.createTempFile(strImageName, ".jpg", fileDirectoty)
        strFilePath = imageFilename.absolutePath
        return imageFilename
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CAMERA && resultCode == RESULT_OK) {
            convertImage(strFilePath)
        }
    }

    private fun convertImage(imageFilePath: String?) {
        if (imageFilePath.isNullOrEmpty()) return

        val imageFile = File(imageFilePath)
        if (imageFile.exists()) {
            val options = BitmapFactory.Options()
            var bitmapImage = BitmapFactory.decodeFile(strFilePath, options)

            try {
                exifInterface = ExifInterface(imageFile.absolutePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            when (orientation) {
                6 -> matrix.postRotate(90f)
                3 -> matrix.postRotate(180f)
                8 -> matrix.postRotate(270f)
            }

            bitmapImage = Bitmap.createBitmap(
                bitmapImage,
                0,
                0,
                bitmapImage.width,
                bitmapImage.height,
                matrix,
                true
            )

            if (bitmapImage != null) {
                val resizeImage = (bitmapImage.height * (512.0 / bitmapImage.width)).toInt()
                val scaledBitmap = Bitmap.createScaledBitmap(bitmapImage, 512, resizeImage, true)
                Glide.with(this)
                    .load(scaledBitmap)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_photo_camera)
                    .into(binding.imageSelfie)
                strBase64Photo = bitmapToBase64(scaledBitmap)
            } else {
                Toast.makeText(
                    this@AbsenActivity,
                    "Ups, foto kamu belum ada!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = intent
            finish()
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val DATA_TITLE = "TITLE"
    }
}