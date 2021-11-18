package com.outsystems.plugins.healthfitness

import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthFitnessManagerMock
import com.outsystems.plugins.healthfitnesslib.store.AdvancedQueryParameters
import com.outsystems.plugins.healthfitnesslib.store.EnumOperationType
import com.outsystems.plugins.healthfitnesslib.store.HealthStore
import com.outsystems.plugins.healthfitnesslib.HealthFitnessError
import org.junit.Assert
import org.junit.Test
import java.util.*

class AdvancedQueryTest {

    @Test
    fun given_InvalidVariable_When_AdvancedQuery_Then_VariableNotAvailableError() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)
        val parameters = AdvancedQueryParameters("Test", Date(), Date(), null, 1, EnumOperationType.SUM.value, null)
        store.advancedQueryAsync(parameters,
            {
                //test fails
                Assert.fail()
            },
            { error ->
                Assert.assertEquals(error.code, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.code)
                Assert.assertEquals(error.message, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.message)
            })
    }

    @Test
    fun given_OperationNotAllowed_When_AdvancedQuery_Then_OperationNotAllowedError() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)

        val parameters = AdvancedQueryParameters("HEART_RATE", Date(), Date(), null, 1, EnumOperationType.SUM.value, null)
        store.advancedQueryAsync(parameters,
            {
                //test fails
                Assert.fail()
            },
            { error ->
                Assert.assertEquals(error.code, HealthFitnessError.OPERATION_NOT_ALLOWED.code)
                Assert.assertEquals(error.message, HealthFitnessError.OPERATION_NOT_ALLOWED.message)
            })
    }

    @Test
    fun given_VariableWithoutPermissions_When_AdvancedQuery_Then_VariableNotAuthorizedError() {
        val googleFitMock = HealthFitnessManagerMock().apply {
            permissionsGranted = false
        }
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)
        val parameters = AdvancedQueryParameters("HEART_RATE", Date(), Date(), null, 1, EnumOperationType.RAW.value, null)

        store.advancedQueryAsync(parameters,
            {
                Assert.fail()
            },
            { error ->
                Assert.assertEquals(error.code, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.code)
                Assert.assertEquals(error.message, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.message)
            })
    }

    @Test
    fun given_ValidVariable_When_AdvancedQuery_Then_SomeError(){
        val googleFitMock = HealthFitnessManagerMock().apply {
            getDataSuccess = false
        }
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)

        val parameters = AdvancedQueryParameters("HEART_RATE", Date(), Date(), null, 1, EnumOperationType.RAW.value, null)

        store.advancedQueryAsync(parameters,
            {
                Assert.fail()
            },
            { error ->
                Assert.assertEquals(error.code, HealthFitnessError.READ_DATA_ERROR.code)
                Assert.assertEquals(error.message, HealthFitnessError.READ_DATA_ERROR.message)
            })
    }

    @Test
    fun given_ValidVariable_When_AdvancedQuery_Then_Success(){

        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)
        val queryDate = Date()
        val parameters = AdvancedQueryParameters("HEART_RATE", queryDate, queryDate, null, 1, EnumOperationType.RAW.value, null)

        store.advancedQueryAsync(parameters,
            { result ->
                Assert.assertTrue(result.results.isNotEmpty())
                Assert.assertTrue(result.results[0].values.isEmpty())

                // Result comes in seconds, instead of milliseconds
                Assert.assertEquals(result.results[0].startDate, queryDate.time / 1000)
                Assert.assertEquals(result.results[0].endDate, queryDate.time / 1000)
            },
            {
                Assert.fail()
            })
    }

}