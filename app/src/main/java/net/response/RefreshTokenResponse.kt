package net.response

data class RefreshTokenResponse(val token: String, val expirationDate: Long)