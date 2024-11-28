package com.ddkric.play2plat.api

data class UserGame (
    val userId: Int,
    val gameId: Int,
    val state: String
)