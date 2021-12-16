package com.outsystems.plugins.healthfitness

import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthFitnessManagerMock
import com.outsystems.plugins.healthfitnesslib.HealthFitnessError
import com.outsystems.plugins.healthfitnesslib.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitnesslib.background.UpdateBackgroundJobParameters
import com.outsystems.plugins.healthfitnesslib.store.HealthStore
import org.junit.Assert
import org.junit.Test

class UpdateBackgroundJobsTest {

    @Test
    fun given_NonExistentBackgroundJob_When_UpdatingBackgroundJob_Then_BackgroundJobDoesNotExistError() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock().apply {
            backgroundJobExists = false
        }
        val store = HealthStore("", googleFitMock, databaseMock)

        val parameters = UpdateBackgroundJobParameters(
            "25aea7f8-9c73-4ca8-97d4-ad63d7e1b854",
            100F,
            "HIGHER",
            "DAY",
            1,
            true,
            "Header",
            "Body"
        )

        store.updateBackgroundJob(parameters,
            onSuccess = {
                Assert.fail()
            },
            onError = { error ->
                Assert.assertEquals(error.code, HealthFitnessError.BACKGROUND_JOB_DOES_NOT_EXISTS_ERROR.code)
                Assert.assertEquals(error.message, HealthFitnessError.BACKGROUND_JOB_DOES_NOT_EXISTS_ERROR.message)
            }
        )
    }

    @Test
    fun given_ExistentBackgroundJob_When_UpdatingBackgroundJob_Then_Success() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock().apply {
            backgroundJobExists = true
        }
        val store = HealthStore("", googleFitMock, databaseMock)

        val parameters = UpdateBackgroundJobParameters(
            "25aea7f8-9c73-4ca8-97d4-ad63d7e1b854",
            100F,
            "HIGHER",
            "DAY",
            1,
            true,
            "Header",
            "Body"
        )

        store.updateBackgroundJob(parameters,
            onSuccess = { response ->
                Assert.assertEquals(response, "success")
            },
            onError = {
                Assert.fail()
            }
        )
    }

}

