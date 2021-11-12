package com.outsystems.plugins.healthfitness

import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthFitnessManagerMock
import com.outsystems.plugins.healthfitnesslib.HealthFitnessError
import com.outsystems.plugins.healthfitnesslib.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitnesslib.store.HealthStore
import org.junit.Assert
import org.junit.Test
import java.lang.AssertionError

class BackgroundJobTest {

    @Test
    fun given_InvalidVariable_When_SettingBackgroundJob_Then_VariableNotAvailableError() {
        //TODO
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
                "Header",
                "Body")

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