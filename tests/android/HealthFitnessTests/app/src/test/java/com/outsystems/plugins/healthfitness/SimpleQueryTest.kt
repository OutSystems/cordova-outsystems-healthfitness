package com.outsystems.plugins.healthfitness

import com.google.gson.Gson
import com.outsystems.plugins.healthfitness.mock.AndroidPlatformMock
import com.outsystems.plugins.healthfitness.mock.HealthFitnessManagerMock
import com.outsystems.plugins.healthfitness.store.AdvancedQueryResponse
import com.outsystems.plugins.healthfitness.store.HealthStore
import org.junit.Assert
import org.junit.Test

class SimpleQueryTest {

    @Test
    fun given_InvalidVariable_When_SimpleQuery_Then_VariableNotAvailableError() {
        val platformInterfaceMock = AndroidPlatformMock().apply {
            sendPluginResultCompletion = { result, error ->
                Assert.assertEquals(result, "null")
                val code = error?.first
                val message = error?.second
                Assert.assertEquals(code, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.code)
                Assert.assertEquals(message, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.message)
            }
        }

        val googleFitMock = HealthFitnessManagerMock()
        val store = HealthStore(platformInterfaceMock, googleFitMock)

        store.getLastRecord("Test")
    }

    @Test
    fun given_VariableWithoutPermissions_When_SimpleQuery_Then_VariableNotAuthorizedError() {
        val platformInterfaceMock = AndroidPlatformMock().apply {
            sendPluginResultCompletion = { result, error ->
                Assert.assertEquals(result, "null")
                val code = error?.first
                val message = error?.second
                Assert.assertEquals(code, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.code)
                Assert.assertEquals(
                    message,
                    HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.message
                )
            }
        }

        val googleFitMock = HealthFitnessManagerMock().apply {
            permissionsGranted = false
        }
        val store = HealthStore(platformInterfaceMock, googleFitMock)

        store.getLastRecord("HEART_RATE")
    }

    @Test
    fun given_ValidVariable_When_SimpleQuery_Then_SomeError(){

        val platformInterfaceMock = AndroidPlatformMock().apply {
            sendPluginResultCompletion = { result, error ->
                Assert.assertEquals(result, "null")
                val code = error?.first
                val message = error?.second
                Assert.assertEquals(code, HealthFitnessError.READ_DATA_ERROR.code)
                Assert.assertEquals(message, HealthFitnessError.READ_DATA_ERROR.message)
            }
        }

        val googleFitMock = HealthFitnessManagerMock().apply {
            getDataSuccess = false
        }

        val store = HealthStore(platformInterfaceMock, googleFitMock)

        store.getLastRecord("HEART_RATE")

    }

    @Test
    fun given_ValidVariable_When_SimpleQuery_Then_Success(){

        val platformInterfaceMock = AndroidPlatformMock().apply {
            sendPluginResultCompletion = { result, _ ->
                val response = Gson().fromJson(result, AdvancedQueryResponse::class.java)
                Assert.assertTrue(response.results.isNotEmpty())
                Assert.assertTrue(response.results[0].values.isEmpty())
            }
        }

        val googleFitMock = HealthFitnessManagerMock().apply {
            getDataSuccess = true
        }

        val store = HealthStore(platformInterfaceMock, googleFitMock)

        store.getLastRecord("HEART_RATE")

    }

}