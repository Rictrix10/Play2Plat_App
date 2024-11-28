package com.ddkric.play2plat.room.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.ddkric.play2plat.room.db.AppDatabase
import com.ddkric.play2plat.room.entities.User
import com.ddkric.play2plat.room.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository
    val readAllUsers: LiveData<List<User>>

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        readAllUsers = repository.readAllUsers
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            repository.addUser(user)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }

    fun getUserByIdUser(idUser: Int): LiveData<User> {
        return repository.getUserByIdUser(idUser)
    }

    fun getUserByEmailAndPassword(email: String, password: String): LiveData<User?> {
        return repository.getUserByEmailAndPassword(email, password)
    }

    fun getAllUsers(): LiveData<List<User>> {
        return repository.readAllUsers
    }
}
