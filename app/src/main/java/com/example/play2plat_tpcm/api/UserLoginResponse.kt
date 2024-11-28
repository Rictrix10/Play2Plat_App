package com.example.play2plat.api

data class UserLoginResponse(
    val message: String,
    val accessToken: String,
    val user: User,
    val refreshToken: String
)
