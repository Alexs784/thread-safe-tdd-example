package repository

import net.api.AuthorizationApi
import storage.DataStorage

class DefaultAuthorizationRepository(
    private val authorizationApi: AuthorizationApi,
    private val dataStorage: DataStorage
) : AuthorizationRepository {


    @Synchronized
    override fun refreshToken(token: String) {
        val result = authorizationApi.refreshToken(token)

        dataStorage.storeToken(result.token)
        dataStorage.storeTokenExpirationDate(result.expirationDate)
    }
}