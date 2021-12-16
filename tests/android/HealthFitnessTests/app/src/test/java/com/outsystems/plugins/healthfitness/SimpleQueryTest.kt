package com.outsystems.plugins.healthfitness

import com.google.gson.Gson
import com.outsystems.plugins.healthfitness.mock.AndroidPlatformMock
import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthFitnessManagerMock
import com.outsystems.plugins.healthfitness.HealthFitnessError
import com.outsystems.plugins.healthfitness.store.AdvancedQueryResponse
import com.outsystems.plugins.healthfitness.store.HealthStore
import org.junit.Assert
import org.junit.Test

class SimpleQueryTest {

    @Test
    fun given_InvalidVariable_When_SimpleQuery_Then_VariableNotAvailableError() {

        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)

        store.getLastRecordAsync("Test",
            {
                //test fails
                Assert.assertTrue(false)
            },
            { error ->
                val code = error.code
                val message = error.message
                Assert.assertEquals(code, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.code)
                Assert.assertEquals(message, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.message)
            })
    }

    @Test
    fun given_VariableWithoutPermissions_When_SimpleQuery_Then_VariableNotAuthorizedError() {

        val googleFitMock = HealthFitnessManagerMock().apply {
            permissionsGranted = false
        }
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)

        store.getLastRecordAsync("HEART_RATE",
            {
                //test fails
                Assert.assertTrue(false)
            },
            { error ->
                val code = error.code
                val message = error.message
                Assert.assertEquals(code, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.code)
                Assert.assertEquals(message, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.message)
            })

    }

    @Test
    fun given_ValidVariable_When_SimpleQuery_Then_SomeError(){

        val googleFitMock = HealthFitnessManagerMock().apply {
            getDataSuccess = false
        }
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)

        store.getLastRecordAsync("HEART_RATE",
            {
                //test fails
                Assert.assertTrue(false)
            },
            { error ->
                val code = error.code
                val message = error.message
                Assert.assertEquals(code, HealthFitnessError.READ_DATA_ERROR.code)
                Assert.assertEquals(message, HealthFitnessError.READ_DATA_ERROR.message)
            })

    }

    @Test
    fun given_ValidVariable_When_SimpleQuery_Then_Success(){

        val googleFitMock = HealthFitnessManagerMock().apply {
            getDataSuccess = true
        }
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)

        store.getLastRecordAsync("HEART_RATE",
            { result ->
                Assert.assertTrue(result.results.isNotEmpty())
                Assert.assertTrue(result.results[0].values.isEmpty())
            },
            {
                //test fails
                Assert.assertTrue(false)
            })


    }

}