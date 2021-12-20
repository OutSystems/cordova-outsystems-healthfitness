package com.outsystems.plugins.healthfitness

import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthStoreMock
import com.outsystems.plugins.healthfitness.background.VariableUpdateManager
import com.outsystems.plugins.healthfitness.background.database.BackgroundJob
import com.outsystems.plugins.healthfitness.store.AdvancedQueryResponse
import com.outsystems.plugins.healthfitness.store.AdvancedQueryResponseBlock
import org.junit.Assert
import org.junit.Test

class NotificationFrequencyTest {

    private val variableName = "STEPS"
    private val responseValue = 10.0f
    private val store = HealthStoreMock().apply {
        advancedQueryResponseForVariable = mapOf(
            Pair(variableName, AdvancedQueryResponse(
                listOf(
                    AdvancedQueryResponseBlock(
                        0,
                        0,
                        0,
                        mutableListOf(responseValue)
                    )
                )
            ))
        )
    }
    private val databaseMock = DatabaseManagerMock()
    private val vum = VariableUpdateManager(variableName, databaseMock, store)

    private fun wasNotificationTriggered(): Boolean {
        var wasCalled = false
        vum.processBackgroundJobs(onSendNotification = { _ ->
            wasCalled = true
        })
        return wasCalled
    }

    @Test
    fun given_frequencyAlways_When_processingBackgroundJobs_Then_notificationSent(){
        databaseMock.apply {
            backgroundJobExists = true
            backgroundJobs.add(
                BackgroundJob().apply {
                    variable = variableName
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "ALWAYS"
                }
            )
        }
        Assert.assertTrue(wasNotificationTriggered())
        Assert.assertTrue(wasNotificationTriggered())
    }

    @Test
    fun given_frequencyDay_When_processingBackgroundJobs_Then_notificationNotSent(){
        databaseMock.apply {
            backgroundJobExists = true
            backgroundJobs.add(
                BackgroundJob().apply {
                    variable = variableName
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "DAY"
                }
            )
        }
        Assert.assertTrue(wasNotificationTriggered())
        //The second one should only be called after 1 day
        Assert.assertFalse(wasNotificationTriggered())
    }

    @Test
    fun given_frequency2Seconds_When_processingBackgroundJobs_Then_notificationSent(){
        databaseMock.apply {
            backgroundJobExists = true
            backgroundJobs.add(
                BackgroundJob().apply {
                    variable = variableName
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "SECOND"
                    notificationFrequencyGrouping = 2
                }
            )
        }
        Assert.assertTrue(wasNotificationTriggered())
        //The second one should NOT be called right after.
        Assert.assertFalse(wasNotificationTriggered())

        Thread.sleep(2000)
        //The third one should be called only after 2 seconds.
        Assert.assertTrue(wasNotificationTriggered())

    }

    @Test
    fun given_inactiveJob_When_processingBackgroundJobs_Then_notificationNotSent(){
        databaseMock.apply {
            backgroundJobExists = true
            backgroundJobs.add(
                BackgroundJob().apply {
                    variable = variableName
                    comparison = BackgroundJob.ComparisonOperationEnum.EQUALS.id
                    value = responseValue
                    notificationId = 0
                    notificationFrequency = "ALWAYS"
                    isActive = false
                }
            )
        }
        Assert.assertFalse(wasNotificationTriggered())
    }
}