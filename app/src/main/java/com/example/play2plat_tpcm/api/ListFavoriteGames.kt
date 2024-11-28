package com.example.play2plat.api

data class ListFavoriteGames (
    val userId: Int,
    val gameId: Int,
    val game: GameFavorite
)