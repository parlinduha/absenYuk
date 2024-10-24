package com.azhar.absensi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.azhar.absensi.database.User


class UserViewModel : ViewModel() {
    private val _users = MutableLiveData<MutableList<User>>()
    val users: LiveData<MutableList<User>> = _users

    init {
        _users.value = mutableListOf(
            User(1, "Azhar", "azhar@example.com"),
            User(2, "Fajar", "fajar@example.com")
        )
    }

    fun addUser(user: User) {
        _users.value?.add(user)
        _users.value = _users.value // Trigger LiveData update
    }

    fun updateUser(user: User) {
        val index = _users.value?.indexOfFirst { it.id == user.id }
        if (index != null && index >= 0) {
            _users.value?.set(index, user)
            _users.value = _users.value
        }
    }

    fun deleteUser(user: User) {
        _users.value?.remove(user)
        _users.value = _users.value
    }
}
