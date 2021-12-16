package com.outsystems.plugins.healthfitness

import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthFitnessManagerMock
import com.outsystems.plugins.healthfitnesslib.HealthFitnessError
import com.outsystems.plugins.healthfitnesslib.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitnesslib.store.HealthStore
import org.junit.Assert
import org.junit.Test

class DeleteBackgroundJobsTest {

    @Test
    fun given_NonExistentBackgroundJob_When_DeletingBackgroundJob_Then_BackgroundJobDoesNotExistError() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock().apply {
            backgroundJobExists = false
        }
        val store = HealthStore("", googleFitMock, databaseMock)

        store.deleteBackgroundJob("25aea7f8-9c73-4ca8-97d4-ad63d7e1b854",
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
    fun given_ExistentBackgroundJob_When_DeletingBackgroundJob_Then_UnsubscribingError() {
        val googleFitMock = HealthFitnessManagerMock().apply {
            unsubscribeError = true
        }
        val databaseMock = DatabaseManagerMock().apply {
            backgroundJobExists = true
        }
        val store = HealthStore("", googleFitMock, databaseMock)

        store.deleteBackgroundJob("35fae7f8-9c73-4ca8-97d4-ad63d7e1b635",
            onSuccess = {
                Assert.fail()
            },
            onError = {error ->
                Assert.assertEquals(error.code, HealthFitnessError.UNSUBSCRIBE_ERROR.code)
                Assert.assertEquals(error.message, HealthFitnessError.UNSUBSCRIBE_ERROR.message)
            })
    }

    @Test
    fun given_ExistentBackgroundJob_When_DeletingBackgroundJob_Then_SomeError() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock().apply {
            backgroundJobExists = true
            databaseHasError = true
        }
        val store = HealthStore("", googleFitMock, databaseMock)

        store.deleteBackgroundJob("35fae7f8-9c73-4ca8-97d4-ad63d7e1b635",
            onSuccess = {
                Assert.fail()
            },
            onError = {error ->
                Assert.assertEquals(error.code, HealthFitnessError.DELETE_BACKGROUND_JOB_GENERIC_ERROR.code)
                Assert.assertEquals(error.message, HealthFitnessError.DELETE_BACKGROUND_JOB_GENERIC_ERROR.message)
            }
        )
    }

    @Test
    fun given_ExistentBackgroundJob_When_DeletingBackgroundJob_Then_Success() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock().apply {
            backgroundJobExists = true
        }
        val store = HealthStore("", googleFitMock, databaseMock)

        store.deleteBackgroundJob("35fae7f8-9c73-4ca8-97d4-ad63d7e1b635",
            onSuccess = { response ->
                Assert.assertEquals(response, "success")
            },
            onError = {
                Assert.fail()
            }
        )
    }

}

