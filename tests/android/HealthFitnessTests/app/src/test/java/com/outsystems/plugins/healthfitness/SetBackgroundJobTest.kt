package com.outsystems.plugins.healthfitness

import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthFitnessManagerMock
import com.outsystems.plugins.healthfitnesslib.HealthFitnessError
import com.outsystems.plugins.healthfitnesslib.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitnesslib.store.HealthStore
import org.junit.Assert
import org.junit.Test

class SetBackgroundJobTest {

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
            "DAY",
            1,
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
        val googleFitMock = HealthFitnessManagerMock().apply {
            permissionsGranted = false
        }
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)

        val parameters = BackgroundJobParameters(
            "HEART_RATE",
            "0",
            "GREATER",
            "TIME",
            1,
            "DAY",
            "DAY",
            1,
            "Header",
            "Body"
        )

        store.setBackgroundJob(parameters,
            {
                //test fails
                Assert.fail()
            },
            { error ->
                Assert.assertEquals(error.code, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.code)
                Assert.assertEquals(error.message, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.message)
            })
    }

    @Test
    fun given_ExistentBackgroundJob_When_SettingBackgroundJob_Then_BackgroundJobAlreadyExistsError() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock().apply {
            backgroundJobAlreadyExists = true
        }
        val store = HealthStore("", googleFitMock, databaseMock)

        val parameters = BackgroundJobParameters(
            "HEART_RATE",
            "0",
            "GREATER",
            "TIME",
            1,
            "DAY",
            "DAY",
            1,
            "Header",
            "Body"
        )

        store.setBackgroundJob(parameters,
            {
                //test fails
                Assert.fail()
            },
            { error ->
                Assert.assertEquals(error.code, HealthFitnessError.BACKGROUND_JOB_ALREADY_EXISTS_ERROR.code)
                Assert.assertEquals(error.message, HealthFitnessError.BACKGROUND_JOB_ALREADY_EXISTS_ERROR.message)
            })
    }

    @Test
    fun given_ValidVariableValidValue_When_SettingBackgroundJob_Then_SomeError() {
        val googleFitMock = HealthFitnessManagerMock().apply {
            backgroundJobSuccess = false
        }
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)
        val parameters =
            BackgroundJobParameters("STEPS",
                "0",
                "GREATER",
                "TIME",
                1,
                "DAY",
                "DAY",
                1,
                "Header",
                "Body"
            )

        store.setBackgroundJob(parameters,
            onSuccess = {
                Assert.fail()
            },
            onError = {error ->
                Assert.assertEquals(error.code, HealthFitnessError.BACKGROUND_JOB_GENERIC_ERROR.code)
                Assert.assertEquals(error.message, HealthFitnessError.BACKGROUND_JOB_GENERIC_ERROR.message)
            })
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
                "DAY",
                1,
                "Header",
                "Body"
            )

        store.setBackgroundJob(parameters,
            onSuccess = { response ->
                Assert.assertEquals(response, "success")
            },
            onError = { error ->
                Assert.fail()
            })
    }

}