package com.azhar.absensi.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azhar.absensi.R
import com.azhar.absensi.model.User
import kotlinx.android.synthetic.main.item_user.view.*

class UserAdapter(private val userList: List<User>, private val onItemClick: (User, String) -> Unit) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = userList.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(user: User) {
            itemView.tvName.text = user.email
            itemView.tvPassword.text = user.password
            itemView.btnEdit.setOnClickListener { onItemClick(user, "edit") }
            itemView.btnDelete.setOnClickListener { onItemClick(user, "delete") }
        }
    }
}
