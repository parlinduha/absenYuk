package com.azhar.absensi.view.history

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ListHistoryAbsenBinding
import com.azhar.absensi.model.ModelDatabase
import com.azhar.absensi.utils.BitmapManager.base64ToBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class HistoryAdapter(
    private val mContext: Context,
    private val modelDatabase: MutableList<ModelDatabase>,
    private val mAdapterCallback: HistoryAdapterCallback
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    fun setDataAdapter(items: List<ModelDatabase>) {
        modelDatabase.clear()
        modelDatabase.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListHistoryAbsenBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = modelDatabase[position]
        with(holder.binding) {
            tvNomor.text = data.uid.toString()
            tvNama.text = data.nama
            tvLokasi.text = data.lokasi
            tvAbsenTime.text = data.tanggal
            tvStatusAbsen.text = data.keterangan

            Glide.with(mContext)
                .load(base64ToBitmap(data.fotoSelfie))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_photo_camera)
                .into(imageProfile)

            colorStatus.setBackgroundResource(R.drawable.bg_circle_radius)
            colorStatus.backgroundTintList = when (data.keterangan) {
                "Absen Masuk" -> ColorStateList.valueOf(Color.GREEN)
                "Absen Keluar" -> ColorStateList.valueOf(Color.RED)
                "Izin" -> ColorStateList.valueOf(Color.BLUE)
                else -> ColorStateList.valueOf(Color.GRAY)
            }
        }
    }

    override fun getItemCount(): Int = modelDatabase.size

    inner class ViewHolder(val binding: ListHistoryAbsenBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.cvHistory.setOnClickListener {
                val modelLaundry = modelDatabase[adapterPosition]
                mAdapterCallback.onDelete(modelLaundry)
            }
        }
    }

    interface HistoryAdapterCallback {
        fun onDelete(modelDatabase: ModelDatabase?)
    }
}