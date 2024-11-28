package com.example.play2plat.api

data class UserComment(
    val id: Int,
    val username: String?,
    val avatar: String?,
    val isDeleted: Boolean
)