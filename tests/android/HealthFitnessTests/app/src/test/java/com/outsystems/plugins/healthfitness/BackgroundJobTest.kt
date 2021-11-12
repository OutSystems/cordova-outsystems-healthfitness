package com.outsystems.plugins.healthfitness

import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthFitnessManagerMock
import com.outsystems.plugins.healthfitnesslib.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitnesslib.HealthFitnessError
import com.outsystems.plugins.healthfitnesslib.store.HealthStore
import org.junit.Assert
import org.junit.Test

class BackgroundJobTest {

    @Test
    fun given_InvalidVariable_When_SettingBackgroundJob_Then_VariableNotAvailableError() {

        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)

        val parameters = BackgroundJobParameters(
            "Test",
            "0",
            "GREATER",
            "TIME",
            1,
            "DAY",
            "Header",
            "Body"
        )

        store.setBackgroundJob(parameters,
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
    fun given_VariableWithoutPermissions_When_SettingBackgroundJob_Then_VariableNotAuthorizedError() {
        //TODO
    }

    @Test
    fun given_ExistentBackgroundJob_When_SettingBackgroundJob_Then_BackgroundJobAlreadyExistsError() {
        //TODO
    }

    @Test
    fun given_ValidVariableValidValue_When_SettingBackgroundJob_Then_SomeError() {
        //TODO
    }

    @Test
    fun given_ValidVariableValidValue_When_SettingBackgroundJob_Then_Success() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)
        val parameters =
            BackgroundJobParameters("STEPS",
                "0",
                "GREATER",
                "TIME",
                1,
                "DAY",
                "Header",
                "Body")

        store.setBackgroundJob(parameters,
            onSuccess = { response ->
                Assert.assertEquals(response, "success")
            },
            onError = {
                Assert.fail()
            })
    }



}