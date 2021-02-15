package repository

import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.inOrder
import kotlinx.coroutines.*
import net.api.AuthorizationApi
import net.response.RefreshTokenResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import storage.DataStorage
import java.util.concurrent.Executors.newFixedThreadPool

class DefaultAuthorizationRepositoryTest {

    private lateinit var authorizationRepository: AuthorizationRepository

    @Mock
    private lateinit var dataStorage: DataStorage

    @Mock
    private lateinit var authorizationApi: AuthorizationApi

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        authorizationRepository = DefaultAuthorizationRepository(authorizationApi, dataStorage)
    }

    @Test
    fun shouldStoreAuthorizationDataAndBeThreadSafe() = runBlocking {
        val currentToken = "currentToken"
        val firstReceivedToken = "firstReceivedToken"
        val firstExpirationDate = 0L
        val firstResponse = RefreshTokenResponse(firstReceivedToken, firstExpirationDate)
        val secondReceivedToken = "secondReceivedToken"
        val secondExpirationDate = 1L
        val secondResponse = RefreshTokenResponse(secondReceivedToken, secondExpirationDate)

        given(authorizationApi.refreshToken(currentToken)).willReturn(firstResponse)
        given(authorizationApi.refreshToken(firstReceivedToken)).willReturn(secondResponse)

        val concurrentCalls = setUpConcurrentCalls(currentToken, firstReceivedToken)
        concurrentCalls.awaitAll()

        inOrder(authorizationApi, dataStorage) {
            verify(authorizationApi).refreshToken(currentToken)
            verify(dataStorage).storeToken(firstReceivedToken)
            verify(dataStorage).storeTokenExpirationDate(firstExpirationDate)

            verify(authorizationApi).refreshToken(firstReceivedToken)
            verify(dataStorage).storeToken(secondReceivedToken)
            verify(dataStorage).storeTokenExpirationDate(secondExpirationDate)
        }
    }

    private fun CoroutineScope.setUpConcurrentCalls(
        currentToken: String,
        firstReceivedToken: String
    ): List<Deferred<Unit>> {
        val coroutinesDispatcher = newFixedThreadPool(2).asCoroutineDispatcher()

        return listOf(
            async(
                context = coroutinesDispatcher,
                start = CoroutineStart.LAZY
            ) { authorizationRepository.refreshToken(currentToken) },
            async(
                context = coroutinesDispatcher,
                start = CoroutineStart.LAZY
            ) { authorizationRepository.refreshToken(firstReceivedToken) }
        )
    }
}