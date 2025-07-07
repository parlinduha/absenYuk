package com.azhar.absensi.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azhar.absensi.databinding.ItemUserBinding
import com.azhar.absensi.model.User

class UserAdapter(
    private val userList: List<User>,
    private val onItemClick: (User, String) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = userList.size

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvName.text = user.email
            binding.tvPassword.text = user.password
            binding.btnEdit.setOnClickListener { onItemClick(user, "edit") }
            binding.btnDelete.setOnClickListener { onItemClick(user, "delete") }
        }
    }
}