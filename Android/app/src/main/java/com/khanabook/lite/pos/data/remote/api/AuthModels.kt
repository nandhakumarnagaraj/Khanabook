package com.khanabook.lite.pos.data.remote.api

data class LoginRequest(
    val email: String,
    val passwordHash: String,
    val deviceId: String
)

data class GoogleLoginRequest(
    val idToken: String,
    val deviceId: String
)

data class SignupRequest(
    val email: String,
    val name: String,
    val passwordHash: String,
    val deviceId: String
)

data class AuthResponse(
    val token: String,
    val restaurantId: Long,
    val userName: String,
    val role: String
)
