package com.outsystems.plugins.healthfitness

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

        val googleFitMock = HealthFitnessManagerMock()
        val store = HealthStore(platformInterfaceMock, googleFitMock)

        googleFitMock.permissionsGranted = false

        store.getLastRecord("HEART_RATE")
    }



}