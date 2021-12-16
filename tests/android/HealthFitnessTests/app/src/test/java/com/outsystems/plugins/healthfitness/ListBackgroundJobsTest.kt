package com.outsystems.plugins.healthfitness

import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthFitnessManagerMock
import com.outsystems.plugins.healthfitness.HealthFitnessError
import com.outsystems.plugins.healthfitness.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitness.store.HealthStore
import org.junit.Assert
import org.junit.Test

class ListBackgroundJobsTest {

    @Test
    fun given_ExistentBackgroundJobs_When_ListingBackgroundJob_Then_SomeError() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock().apply {
            databaseHasError = true
        }
        val store = HealthStore("", googleFitMock, databaseMock)

        store.listBackgroundJobs(
            onSuccess = {
                Assert.fail()
            },
            onError = {error ->
                Assert.assertEquals(error.code, HealthFitnessError.LIST_BACKGROUND_JOBS_GENERIC_ERROR.code)
                Assert.assertEquals(error.message, HealthFitnessError.LIST_BACKGROUND_JOBS_GENERIC_ERROR.message)
            })
    }

    @Test
    fun given_NoBackgroundJobs_When_ListingBackgroundJob_Then_Success() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)

        store.listBackgroundJobs(
            onSuccess = { response ->
                Assert.assertTrue(response.results.isEmpty())
                Assert.assertEquals(response.results.size, 0)
            },
            onError = {
                Assert.fail()
            }
        )
    }

    @Test
    fun given_ExistentBackgroundJobs_When_ListingBackgroundJob_Then_Success() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock().apply {
            hasBackgroundJobs = true
        }
        val store = HealthStore("", googleFitMock, databaseMock)

        store.listBackgroundJobs(
            onSuccess = { response ->
                Assert.assertTrue(response.results.isNotEmpty())
                Assert.assertEquals(response.results.size, 1)
            },
            onError = {
                Assert.fail()
            }
        )
    }

}

