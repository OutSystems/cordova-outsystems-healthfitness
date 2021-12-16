package com.outsystems.plugins.healthfitnesslib.background

import android.util.Log
import com.outsystems.plugins.healthfitnesslib.background.database.DatabaseManagerInterface
import com.outsystems.plugins.healthfitnesslib.background.database.BackgroundJob
import com.outsystems.plugins.healthfitnesslib.background.database.Notification
import com.outsystems.plugins.healthfitnesslib.store.AdvancedQueryParameters
import com.outsystems.plugins.healthfitnesslib.store.HealthStoreInterface
import java.util.*

class VariableUpdateManager(
    private val variableName: String,
    private val database: DatabaseManagerInterface,
    private val heathStore: HealthStoreInterface) {

    fun processBackgroundJobs(onSendNotification: (notification: Notification) -> Unit){

        val operationType : String
        val variable = heathStore.getVariableByName(variableName)

        operationType = if(variable?.allowedOperations?.contains("SUM") == true){
            "SUM"
        } else{
            "RAW"
        }
        val backgroundJobs = database.fetchBackgroundJobs(variableName)
        backgroundJobs?.forEach { job ->
            val currentTimestamp = System.currentTimeMillis()
            val nextNotificationTimestamp = job.nextNotificationTimestamp

            if(job.isActive && (currentTimestamp >= nextNotificationTimestamp || job.notificationFrequency == "ALWAYS")) {

                val nextNotificationCalendar = Calendar.getInstance()
                nextNotificationCalendar.timeInMillis = currentTimestamp
                nextNotificationCalendar.minimalDaysInFirstWeek = 4
                nextNotificationCalendar.firstDayOfWeek = Calendar.MONDAY

                if(job.notificationFrequency == "SECOND") {
                    nextNotificationCalendar.add(Calendar.SECOND, job.notificationFrequencyGrouping)
                }
                else if(job.notificationFrequency == "MINUTE") {
                    nextNotificationCalendar.add(Calendar.MINUTE, job.notificationFrequencyGrouping)
                }
                else if(job.notificationFrequency == "HOUR") {
                    nextNotificationCalendar.add(Calendar.HOUR, job.notificationFrequencyGrouping)
                    nextNotificationCalendar.startOfUnit(Calendar.HOUR)
                }
                else if(job.notificationFrequency == "DAY") {
                    nextNotificationCalendar.add(Calendar.DATE, job.notificationFrequencyGrouping)
                    nextNotificationCalendar.startOfUnit(Calendar.DATE)
                }
                else if(job.notificationFrequency == "WEEK") {
                    nextNotificationCalendar.add(Calendar.WEEK_OF_MONTH, job.notificationFrequencyGrouping)
                    nextNotificationCalendar.startOfUnit(Calendar.WEEK_OF_MONTH)
                }
                else if(job.notificationFrequency == "MONTH") {
                    nextNotificationCalendar.add(Calendar.MONTH, job.notificationFrequencyGrouping)
                    nextNotificationCalendar.startOfUnit(Calendar.MONTH)
                }
                else if (job.notificationFrequency == "YEAR") {
                    nextNotificationCalendar.add(Calendar.YEAR, job.notificationFrequencyGrouping)
                    nextNotificationCalendar.startOfUnit(Calendar.YEAR)
                }

                job.nextNotificationTimestamp = nextNotificationCalendar.timeInMillis

                database.updateBackgroundJob(job)
                job.notificationId?.let { notificationId ->
                    database.fetchNotification(notificationId)?.let { notification ->

                        val endDate: Long = Date().time
                        val month = 2592000000
                        val startDate: Long = endDate - month

                        val queryParams =  AdvancedQueryParameters(
                            variableName,
                            Date(startDate),
                            Date(endDate),
                            job.timeUnit,
                            job.timeUnitGrouping,
                            operationType
                        )
                        heathStore.advancedQueryAsync(
                            queryParams,
                            { response ->
                                var willTriggerJob = false
                                if(response.results.isNotEmpty()) {
                                    val comparison = job.comparison
                                    val triggerValue = job.value
                                    val currentValue = response.results.last().values.last()
                                    when(comparison){
                                        BackgroundJob.ComparisonOperationEnum.EQUALS.id ->
                                            willTriggerJob = currentValue == triggerValue
                                        BackgroundJob.ComparisonOperationEnum.GREATER.id ->
                                            willTriggerJob = currentValue > triggerValue
                                        BackgroundJob.ComparisonOperationEnum.LESSER.id ->
                                            willTriggerJob = currentValue < triggerValue
                                        BackgroundJob.ComparisonOperationEnum.GREATER_OR_EQUALS.id ->
                                            willTriggerJob = currentValue >= triggerValue
                                        BackgroundJob.ComparisonOperationEnum.LESSER_OR_EQUALS.id ->
                                            willTriggerJob = currentValue <= triggerValue
                                    }
                                }
                                if(willTriggerJob){
                                    onSendNotification(notification)
                                }
                            },
                            { error ->
                                Log.e("Err", error.message)
                                //TODO: What should we do with errors?
                            }
                        )
                    }
                }
            }
        }
    }

    private fun Calendar.startOfUnit(unit: Int): Calendar {
        var unitVal = 0
        when(unit){
            Calendar.HOUR -> unitVal = 1
            Calendar.DATE -> unitVal = 2
            Calendar.WEEK_OF_MONTH -> unitVal = 3
            Calendar.MONTH -> unitVal = 4
            Calendar.YEAR -> unitVal = 5
        }

        if(unitVal >= 5){
            this.set(Calendar.MONTH, 0)
        }
        if(unitVal >= 4){
            this.set(Calendar.DAY_OF_MONTH, 1)
        }
        if(unitVal == 3){
            this.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        if(unitVal >= 2){
            this.set(Calendar.HOUR_OF_DAY, 0)
        }
        if(unitVal >= 1){
            this.set(Calendar.MILLISECOND, 0)
            this.set(Calendar.SECOND, 0)
            this.set(Calendar.MINUTE, 0)
        }

        return this
    }
}