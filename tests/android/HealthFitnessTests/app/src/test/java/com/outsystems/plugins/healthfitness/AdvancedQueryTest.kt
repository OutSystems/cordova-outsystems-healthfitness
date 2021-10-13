package com.outsystems.plugins.healthfitness

import com.outsystems.plugins.healthfitness.store.AdvancedQueryParameters
import com.outsystems.plugins.healthfitness.store.EnumOperationType
import com.outsystems.plugins.healthfitness.store.EnumTimeUnit
import com.outsystems.plugins.healthfitness.store.HealthStore
import org.junit.Assert
import org.junit.Test
import java.util.*

class AdvancedQueryTest {

    @Test
    fun given_InvalidVariable_When_AdvancedQuery_Then_VariableNotAvailableError() {
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

        val parameters = AdvancedQueryParameters("Test", Date(), Date(), null, 1, EnumOperationType.SUM.value, null)
        store.advancedQuery(parameters)
    }

    @Test
    fun given_OperationNotAllowed_When_AdvancedQuery_Then_OperationNotAllowedError() {
        val platformInterfaceMock = AndroidPlatformMock().apply {
            sendPluginResultCompletion = { result, error ->
                Assert.assertEquals(result, "null")
                val code = error?.first
                val message = error?.second
                Assert.assertEquals(code, HealthFitnessError.OPERATION_NOT_ALLOWED.code)
                Assert.assertEquals(message, HealthFitnessError.OPERATION_NOT_ALLOWED.message)
            }
        }

        val googleFitMock = HealthFitnessManagerMock()
        val store = HealthStore(platformInterfaceMock, googleFitMock)

        val parameters = AdvancedQueryParameters("HEART_RATE", Date(), Date(), null, 1, EnumOperationType.SUM.value, null)
        store.advancedQuery(parameters)
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

        val parameters = AdvancedQueryParameters("HEART_RATE", Date(), Date(), null, 1, EnumOperationType.RAW.value, null)
        store.advancedQuery(parameters)
    }

}