package com.azhar.absensi.view.history

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.azhar.absensi.R
import com.azhar.absensi.model.ModelDatabase
import com.azhar.absensi.utils.BitmapManager.base64ToBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.android.synthetic.main.list_history_absen.view.*

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_history_absen, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = modelDatabase[position]
        holder.tvNomor.text = data.uid.toString() // Ganti String.valueOf dengan toString()
        holder.tvNama.text = data.nama
        holder.tvLokasi.text = data.lokasi
        holder.tvAbsenTime.text = data.tanggal
        holder.tvStatusAbsen.text = data.keterangan

        // Gunakan Glide untuk memuat gambar
        Glide.with(mContext)
            .load(base64ToBitmap(data.fotoSelfie))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_photo_camera)
            .into(holder.imageProfile)

        // Mengubah warna status berdasarkan keterangan
        holder.colorStatus.setBackgroundResource(R.drawable.bg_circle_radius)
        holder.colorStatus.backgroundTintList = when (data.keterangan) {
            "Absen Masuk" -> ColorStateList.valueOf(Color.GREEN)
            "Absen Keluar" -> ColorStateList.valueOf(Color.RED)
            "Izin" -> ColorStateList.valueOf(Color.BLUE)
            else -> ColorStateList.valueOf(Color.GRAY) // Default color jika tidak ada yang cocok
        }
    }

    override fun getItemCount(): Int {
        return modelDatabase.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStatusAbsen: TextView = itemView.tvStatusAbsen
        val tvNomor: TextView = itemView.tvNomor
        val tvNama: TextView = itemView.tvNama
        val tvLokasi: TextView = itemView.tvLokasi
        val tvAbsenTime: TextView = itemView.tvAbsenTime
        val cvHistory: CardView = itemView.cvHistory
        val imageProfile: ShapeableImageView = itemView.imageProfile
        val colorStatus: View = itemView.colorStatus

        init {
            cvHistory.setOnClickListener {
                val modelLaundry = modelDatabase[adapterPosition]
                mAdapterCallback.onDelete(modelLaundry)
            }
        }
    }

    interface HistoryAdapterCallback {
        fun onDelete(modelDatabase: ModelDatabase?)
    }
}
