package net.api

import net.response.RefreshTokenResponse

interface AuthorizationApi {

    fun refreshToken(token: String): RefreshTokenResponse
}