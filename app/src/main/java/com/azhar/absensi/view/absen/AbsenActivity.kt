package com.azhar.absensi.view.absen

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import androidx.core.graphics.scale

class AbsenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbsenBinding
    private var REQ_CAMERA = 101
    private var strCurrentLatitude = 0.0
    private var strCurrentLongitude = 0.0
    private var strFilePath: String = ""
    private var strLatitude = "0"
    private var strLongitude = "0"
    private lateinit var currentPhotoFile: File
    private lateinit var exifInterface: ExifInterface
    private lateinit var strBase64Photo: String
    private lateinit var strCurrentLocation: String
    private lateinit var strTitle: String
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
        Log.d("AbsenActivity", "User email: $userEmail")

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
            progressDialog.dismiss()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                progressDialog.dismiss()
                if (location != null) {
                    strCurrentLatitude = location.latitude
                    strCurrentLongitude = location.longitude
                    Log.d("AbsenActivity", "Location: $strCurrentLatitude, $strCurrentLongitude")

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
                            Log.d("AbsenActivity", "Address: $strCurrentLocation")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.e("AbsenActivity", "Geocoder error: ${e.message}")
                    }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@AbsenActivity,
                        "Gagal mendapatkan lokasi. Periksa GPS atau koneksi internet!",
                        Toast.LENGTH_SHORT
                    ).show()
                    strLatitude = "0"
                    strLongitude = "0"
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.e("AbsenActivity", "Location error: ${e.message}")
                Toast.makeText(
                    this@AbsenActivity,
                    "Error mendapatkan lokasi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
            openCamera()
        }
    }

    private fun openCamera() {
        val requiredPermissions = mutableListOf(Manifest.permission.CAMERA)

        // Tambahkan izin penyimpanan berdasarkan versi Android
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            // Android 9 (Pie) dan di bawahnya
            requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            // Android 10-11 (Q-R)
            requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (Tiramisu)
            requiredPermissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        }

        Dexter.withContext(this)
            .withPermissions(*requiredPermissions.toTypedArray())
            .withListener(object : MultiplePermissionsListener {
                @RequiresApi(Build.VERSION_CODES.M)
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        dispatchTakePictureIntent()
                    } else {
                        val denied = report.deniedPermissionResponses
                            .map { it.permissionName }
                        Log.e("Permission", "Denied: $denied")

                        if (denied.isNotEmpty()) {
                            if (shouldShowRequestPermissionRationale(denied.first())) {
                                showPermissionRationaleDialog(denied)
                            } else {
                                showGoToSettingsDialog()
                            }
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

    private fun showPermissionRationaleDialog(deniedPermissions: List<String>) {
        val message = when {
            deniedPermissions.contains(Manifest.permission.CAMERA) &&
                    deniedPermissions.any { it.startsWith("android.permission.READ") } ->
                "Aplikasi membutuhkan akses kamera dan penyimpanan untuk mengambil dan menyimpan foto absensi"

            deniedPermissions.contains(Manifest.permission.CAMERA) ->
                "Aplikasi membutuhkan akses kamera untuk mengambil foto absensi"

            deniedPermissions.any { it.startsWith("android.permission.READ") } ->
                "Aplikasi membutuhkan akses penyimpanan untuk menyimpan foto absensi"

            else -> "Izin diperlukan untuk melanjutkan"
        }

        AlertDialog.Builder(this)
            .setTitle("Izin Diperlukan")
            .setMessage(message)
            .setPositiveButton("Coba Lagi") { _, _ ->
                openCamera()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e("AbsenActivity", "Error creating file: ${ex.message}")
                    Toast.makeText(
                        this@AbsenActivity,
                        "Gagal membuat file untuk menyimpan foto",
                        Toast.LENGTH_SHORT
                    ).show()
                    null
                }

                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this@AbsenActivity,
                        "${BuildConfig.APPLICATION_ID}.provider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivityForResult(takePictureIntent, REQ_CAMERA)
                }
            } ?: run {
                Toast.makeText(
                    this@AbsenActivity,
                    "Tidak ada aplikasi kamera yang ditemukan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Gunakan direktori Pictures yang spesifik untuk aplikasi
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Absensi").apply {
                if (!exists()) mkdirs()
            }
        } else {
            // Untuk Android 9 dan di bawahnya, gunakan direktori Pictures umum
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).apply {
                if (!exists()) mkdirs()
            }
        }

        return File.createTempFile(
            "ABSEN_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            strFilePath = absolutePath
            currentPhotoFile = this
            Log.d("AbsenActivity", "File created: $strFilePath")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("AbsenActivity", "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        if (requestCode == REQ_CAMERA && resultCode == RESULT_OK) {
            Log.d("AbsenActivity", "Camera result OK, file path: $strFilePath")

            if (strFilePath.isNotEmpty()) {
                val file = File(strFilePath)
                if (file.exists()) {
                    Log.d("AbsenActivity", "File exists, size: ${file.length()} bytes")
                    convertImage(strFilePath)
                } else {
                    Log.e("AbsenActivity", "File does not exist at path: $strFilePath")
                    Toast.makeText(
                        this@AbsenActivity,
                        "Gagal menyimpan foto",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Log.e("AbsenActivity", "Empty file path")
                Toast.makeText(
                    this@AbsenActivity,
                    "Gagal mendapatkan path foto",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.d("AbsenActivity", "Camera cancelled")
        } else {
            Log.e("AbsenActivity", "Camera error, result code: $resultCode")
        }
    }

    private fun convertImage(imageFilePath: String) {
        // Cek izin baca sebelum mengakses file
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, storagePermission)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("AbsenActivity", "Permission denied when trying to read image")
            Toast.makeText(this, "Izin membaca penyimpanan ditolak", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            var bitmapImage = BitmapFactory.decodeFile(imageFilePath, options)

            if (bitmapImage == null) {
                Log.e("AbsenActivity", "Bitmap is null after decoding")
                Toast.makeText(this, "Gagal memuat foto", Toast.LENGTH_SHORT).show()
                return
            }

            // Handle orientation
            val exif = ExifInterface(imageFilePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val matrix = Matrix()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
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

            // Calculate aspect ratio
            val aspectRatio = bitmapImage.width.toFloat() / bitmapImage.height.toFloat()
            val targetWidth = 512
            val targetHeight = (targetWidth / aspectRatio).toInt()

            val scaledBitmap = bitmapImage.scale(targetWidth, targetHeight)

            // Set image preview
            Glide.with(this)
                .load(scaledBitmap)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_photo_camera)
                .into(binding.imageSelfie)

            // Convert to base64 and save to variable
            strBase64Photo = bitmapToBase64(scaledBitmap)
            Log.d("AbsenActivity", "Image converted successfully. Base64 length: ${strBase64Photo.length}")

        } catch (e: Exception) {
            Log.e("AbsenActivity", "Error converting image: ${e.message}")
            e.printStackTrace()
            Toast.makeText(
                this@AbsenActivity,
                "Error memproses foto: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setUploadData() {
        binding.btnAbsen.setOnClickListener {
            val strNama = binding.inputNama.text.toString().trim()
            val strTanggal = binding.inputTanggal.text.toString().trim()
            val strKeterangan = binding.inputKeterangan.text.toString().trim()

            if (strFilePath.isEmpty()) {
                Toast.makeText(
                    this@AbsenActivity,
                    "Silakan ambil foto selfie terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (strNama.isEmpty() || strTanggal.isEmpty() || strKeterangan.isEmpty()) {
                Toast.makeText(
                    this@AbsenActivity,
                    "Semua data harus diisi",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!this::strBase64Photo.isInitialized || strBase64Photo.isEmpty()) {
                Log.e("AbsenActivity", "strBase64Photo not initialized or empty")
                Toast.makeText(
                    this@AbsenActivity,
                    "Foto belum diproses, silakan coba lagi",
                    Toast.LENGTH_SHORT
                ).show()

                // Coba konversi ulang jika file masih ada
                if (strFilePath.isNotEmpty() && File(strFilePath).exists()) {
                    Log.d("AbsenActivity", "Attempting to reconvert image")
                    convertImage(strFilePath)
                }
                return@setOnClickListener
            }

            progressDialog.show()
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
                "Absensi berhasil dikirim",
                Toast.LENGTH_SHORT
            ).show()
            progressDialog.dismiss()
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQ_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    openCamera()
                } else {
                    val deniedPermissions = permissions.zip(grantResults.toList())
                        .filter { it.second != PackageManager.PERMISSION_GRANTED }
                        .map { it.first }

                    if (deniedPermissions.any { shouldShowRequestPermissionRationale(it) }) {
                        showPermissionRationaleDialog(deniedPermissions)
                    } else {
                        showGoToSettingsDialog()
                    }
                }
            }
        }
    }

    private fun showGoToSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Izin Diperlukan")
            .setMessage("Anda telah menolak izin secara permanen. Silakan aktifkan izin di Pengaturan Aplikasi")
            .setPositiveButton("Buka Pengaturan") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .show()
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