package com.outsystems.plugins.healthfitnesslib.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.outsystems.plugins.healthfitnesslib.background.database.BackgroundJob
import com.outsystems.plugins.healthfitnesslib.store.AdvancedQueryParameters
import com.outsystems.plugins.healthfitnesslib.store.HealthFitnessManager
import com.outsystems.plugins.healthfitnesslib.store.HealthStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class VariableUpdateService : BroadcastReceiver() {

    private val CHANNEL_ID = "com.outsystems.healthfitness"

    override fun onReceive(context: Context?, intent: Intent?) {
        if(context != null && intent != null) {
            runBlocking {
                launch(Dispatchers.IO) {
                    processBackgroundJobs(context, intent)
                }
            }
        }
    }

    private fun processBackgroundJobs(context : Context, intent : Intent) {

        val variableName = intent.getStringExtra(VARIABLE_NAME) ?: return
        val manager = HealthFitnessManager(context)
        val database = DatabaseManager(context)
        val store = HealthStore(context.applicationContext.packageName, manager, database)

        val operationType : String
        val variable = store.getVariableByName(variableName)

        operationType = if(variable?.allowedOperations?.contains("SUM") == true){
            "SUM"
        } else{
            "RAW"
        }

        val db = DatabaseManager.getInstance(context)
        val backgroundJobs = db.fetchBackgroundJobs(variableName)

        backgroundJobs?.forEach { job ->

            val currentTimestamp = System.currentTimeMillis()
            val nextNotificationTimestamp = job.nextNotificationTimestamp

            if(currentTimestamp >= nextNotificationTimestamp || job.notificationFrequency == "ALWAYS") {

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
                    db.fetchNotification(notificationId)?.let { notification ->

                        val notificationTitle = notification.title
                        val notificationBody = notification.body
                        val notificationID = notification.notificationID

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
                        store.advancedQueryAsync(
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
                                    sendNotification(context, notificationTitle, notificationBody, notificationID)
                                }
                            },
                            { error ->
                                //TODO: What should we do with errors?
                            }
                        )
                    }
                }
            }
        }
    }

    private fun sendNotification(context : Context, title : String, body : String, notificationID : Int) {

        if (Build.VERSION.SDK_INT >= 26) {
            //if the condition is met, then we send the notification
            NotificationManagerCompat.from(context).createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Health & Fitness Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        //build intent to call the ClickActivity
        val myIntent = Intent(context, ClickActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 1, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //get icon for notification
        val icon = getResourceId(context, "mipmap/ic_launcher")

        //here we get the NotificationTitle and NotificationBody from the db
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(icon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationID, notification)
    }

    private fun getResourceId(context: Context, typeAndName: String): Int {
        return context.resources.getIdentifier(typeAndName, null, context.packageName)

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

    companion object {
        const val VARIABLE_NAME = "VARIABLE_NAME"
    }

}