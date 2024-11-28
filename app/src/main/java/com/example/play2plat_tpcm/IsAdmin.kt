package com.ddkric.play2plat

class IsAdmin(private var adminStatus: Boolean) {

    fun isAdmin(): Boolean {
        return adminStatus
    }

    fun setAdmin(admin: Boolean) {
        adminStatus = admin
    }
}
