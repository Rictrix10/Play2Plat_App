package com.example.play2plat.api

data class UserRegister(
    val username: String,
    val email: String,
    val password: String,
    val userTypeId: Int
)
