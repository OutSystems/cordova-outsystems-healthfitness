package com.outsystems.plugins.healthfitness

import com.outsystems.plugins.healthfitness.store.HealthFitnessManagerInterface
import com.outsystems.plugins.healthfitness.store.HealthStore
import org.junit.Assert
import org.junit.Test

class WriteProfileDataTest {

    @Test
    fun given_InvalidVariable_When_WritingData_Then_VariableNotAvailableError() {
        val platformInterfaceMock = AndroidPlatformMock()
        platformInterfaceMock.sendPluginResultCompletion = { result, error ->
            Assert.assertEquals(result, "null")
            val code = error?.first
            val message = error?.second
            Assert.assertEquals(code, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.code)
            Assert.assertEquals(message, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.message)
        }

        val googleFitMock = HealthFitnessManagerMock()
        val store = HealthStore(platformInterfaceMock, googleFitMock)

        store.updateData("Test", 120F)
    }

    @Test
    fun given_VariableWithoutPermissions_When_WritingData_Then_VariableNotAuthorizedError() {
        val platformInterfaceMock = AndroidPlatformMock()
        platformInterfaceMock.sendPluginResultCompletion = { result, error ->
            Assert.assertEquals(result, "null")
            val code = error?.first
            val message = error?.second
            Assert.assertEquals(code, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.code)
            Assert.assertEquals(message, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.message)
        }

        val googleFitMock = HealthFitnessManagerMock()
        val store = HealthStore(platformInterfaceMock, googleFitMock)

        googleFitMock.permissionsGranted = false

        store.updateData("HEIGHT", 170F)
    }

    @Test
    fun given_ValueOutOfRange_When_WritingData_Then_WriteValueOutOfRangeError() {
        TODO("Not yet implemented")
    }

    @Test
    fun given_ValidVariableValidValue_When_WritingData_Then_Success() {
        TODO("Not yet implemented")
    }

}