package com.example.play2plat_tpcm.api

data class UserLoginResponse(
    val message: String,
    val accessToken: String,
    val user: User,
    val refreshToken: String
)
