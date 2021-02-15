package storage

interface DataStorage {

    fun storeToken(token: String)

    fun storeTokenExpirationDate(expirationDate: Long)
}