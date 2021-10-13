package com.outsystems.plugins.healthfitness

import com.outsystems.plugins.healthfitness.store.HealthStore
import org.junit.Assert
import org.junit.Test

class WriteProfileDataTest {

    @Test
    fun given_InvalidVariable_When_WritingData_Then_VariableNotAvailableError() {
        val platformInterfaceMock = AndroidPlatformMock.Builder()
            .pluginResultCompletion { result, error ->
                Assert.assertEquals(result, "null")
                val code = error?.first
                val message = error?.second
                Assert.assertEquals(code, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.code)
                Assert.assertEquals(message, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.message)
            }
            .build()

        val googleFitMock = HealthFitnessManagerMock.Builder()
            .build()

        val store = HealthStore(platformInterfaceMock, googleFitMock)

        store.updateData("Test", 120F)
    }

    @Test
    fun given_VariableWithoutPermissions_When_WritingData_Then_VariableNotAuthorizedError() {

        val platformInterfaceMock = AndroidPlatformMock.Builder()
            .pluginResultCompletion { result, error ->
                Assert.assertEquals(result, "null")
                val code = error?.first
                val message = error?.second
                Assert.assertEquals(code, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.code)
                Assert.assertEquals(message, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.message) }
            .build()

        val googleFitMock = HealthFitnessManagerMock.Builder()
            .permissionsAreGranted(false)
            .build()

        val store = HealthStore(platformInterfaceMock, googleFitMock)
        store.updateData("HEIGHT", 170F)
    }

    @Test
    fun given_ValueOutOfRange_When_WritingData_Then_WriteValueOutOfRangeError() {
        val platformInterfaceMock = AndroidPlatformMock.Builder()
            .pluginResultCompletion { result, error ->
                Assert.assertEquals(result, "null")
                val code = error?.first
                val message = error?.second
                Assert.assertEquals(code, HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR.code)
                Assert.assertEquals(
                    message,
                    HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR.message
                )
            }
            .build()

        val googleFitMock = HealthFitnessManagerMock.Builder()
            .permissionsAreGranted(true)
            .build()

        val store = HealthStore(platformInterfaceMock, googleFitMock)

        store.updateData("HEIGHT", 1000F)
    }

    @Test
    fun given_ValidVariableValidValue_When_WritingData_Then_Success() {
        val platformInterfaceMock = AndroidPlatformMock.Builder()
            .pluginResultCompletion { result, _ ->
                Assert.assertEquals(result, "success")
            }
            .build()

        val googleFitMock = HealthFitnessManagerMock.Builder()
            .build()

        val store = HealthStore(platformInterfaceMock, googleFitMock)

        store.updateData("HEIGHT", 170F)
    }

    @Test
    fun given_ValidVariableValidValue_When_WritingData_Then_SomeError() {

        val platformInterfaceMock = AndroidPlatformMock.Builder()
            .pluginResultCompletion { result, error ->
                Assert.assertEquals(result, "null")
                val code = error?.first
                val message = error?.second
                Assert.assertEquals(code, HealthFitnessError.WRITE_DATA_ERROR.code)
                Assert.assertEquals(message, HealthFitnessError.WRITE_DATA_ERROR.message)
            }
            .build()

        val googleFitMock = HealthFitnessManagerMock.Builder()
            .successOnUpdate(false)
            .build()

        val store = HealthStore(platformInterfaceMock, googleFitMock)

        store.updateData("HEIGHT", 170F)
    }

}