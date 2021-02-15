package repository

interface AuthorizationRepository {

    fun refreshToken(token: String)
}