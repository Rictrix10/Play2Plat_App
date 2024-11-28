package com.ddkric.play2plat.room.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.ddkric.play2plat.room.db.AppDatabase
import com.ddkric.play2plat.room.entities.Game
import com.ddkric.play2plat.room.repository.GameRepository
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    val readAllGames: LiveData<List<Game>>

    init {
        val gameDao = AppDatabase.getDatabase(application).gameDao()
        repository = GameRepository(gameDao)
        readAllGames = repository.readAllGames
    }

    fun addGame(game: Game) {
        viewModelScope.launch {
            repository.addGame(game)
        }
    }

    fun updateGame(game: Game) {
        viewModelScope.launch {
            repository.updateGame(game)
        }
    }

    fun deleteGame(game: Game) {
        viewModelScope.launch {
            repository.deleteGame(game)
        }
    }

    fun getGamesByIdUser(userId: Int): LiveData<List<Game>> {
        return repository.getGamesByIdUser(userId)
    }

    fun getGamesByStateUserId(userId: Int, state: String): LiveData<List<Game>> {
        return repository.getGamesByStateUserId(userId, state)
    }

    fun getGamesByIsFavoriteUserId(userId: Int, isFavorite: Boolean): LiveData<List<Game>> {
        return repository.getGamesByIsFavoriteUserId(userId, isFavorite)
    }
}
