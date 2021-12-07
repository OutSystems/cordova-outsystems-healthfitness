package com.outsystems.plugins.healthfitnesslib.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import com.outsystems.plugins.healthfitnesslib.background.database.AppDatabase
import com.outsystems.plugins.healthfitnesslib.background.database.BackgroundJob
import com.outsystems.plugins.healthfitnesslib.store.AdvancedQueryParameters
import com.outsystems.plugins.healthfitnesslib.store.HealthFitnessManager
import com.outsystems.plugins.healthfitnesslib.store.HealthStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

            //notificationFrequency = SECOND
            //notificationFrequencyGrouping = 30
            //waitingPeriod = 30 seconds = 30*1000 millis

            //notificationFrequency = DAY
            //notificationFrequencyGrouping = 1
            //waitingPeriod = 1 day = 24*60*60*1000 millis

            //check waiting period, only do query after checking that
            if(System.currentTimeMillis() - job.lastNotificationTimestamp!! >= (job.waitingPeriod!!)){

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

    companion object {
        const val VARIABLE_NAME = "VARIABLE_NAME"
    }

}