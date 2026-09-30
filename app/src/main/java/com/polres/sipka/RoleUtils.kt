package com.polres.sipka

fun LoginResponse.isAdmin(): Boolean {
    val value = role?.lowercase().orEmpty()
    return value.contains("admin") || value.contains("atasan")
            || value.contains("kepala") || value.contains("pimpinan")
}
