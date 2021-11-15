package com.outsystems.plugins.healthfitness

import com.outsystems.plugins.healthfitness.mock.AndroidPlatformMock
import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthFitnessManagerMock
import com.outsystems.plugins.healthfitnesslib.HealthFitnessError
import com.outsystems.plugins.healthfitnesslib.store.HealthStore
import org.junit.Assert
import org.junit.Test

class WriteProfileDataTest {

    @Test
    fun given_InvalidVariable_When_WritingData_Then_VariableNotAvailableError() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)
        store.updateDataAsync("Test", 120F,
            onSuccess = {
                Assert.fail()
            },
            onError = { error ->
                Assert.assertEquals(error.code, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.code)
                Assert.assertEquals(error.message, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.message)
            }
        )
    }

    @Test
    fun given_VariableWithoutPermissions_When_WritingData_Then_VariableNotAuthorizedError() {
        val googleFitMock = HealthFitnessManagerMock().apply {
            permissionsGranted = false
        }
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)
        store.updateDataAsync("HEIGHT", 170F,
            onSuccess = {
                Assert.fail()
            },
            onError = { error ->
                Assert.assertEquals(error.code, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.code)
                Assert.assertEquals( error.message, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.message)
            }
        )
    }

    @Test
    fun given_ValueOutOfRange_When_WritingData_Then_WriteValueOutOfRangeError() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)
        store.updateDataAsync("HEIGHT", 1000F,
        onSuccess = {
            Assert.fail()
        },
        onError = { error ->
            Assert.assertEquals(error.code, HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR.code)
            Assert.assertEquals(error.message, HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR.message)
        })
    }

    @Test
    fun given_ValidVariableValidValue_When_WritingData_Then_Success() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)
        store.updateDataAsync("HEIGHT", 170F,
        onSuccess = { result ->
            Assert.assertEquals(result, "success")
        },
        onError = {
            Assert.fail()
        })
    }

    @Test
    fun given_ValidVariableValidValue_When_WritingData_Then_SomeError() {
        val googleFitMock = HealthFitnessManagerMock().apply {
            updateDataSuccess = false
        }
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)
        store.updateDataAsync("HEIGHT", 170F,
        onSuccess = {
            Assert.fail()
        },
        onError = { error ->
            Assert.assertEquals(error.code, HealthFitnessError.WRITE_DATA_ERROR.code)
            Assert.assertEquals(error.message, HealthFitnessError.WRITE_DATA_ERROR.message)
        })
    }

}