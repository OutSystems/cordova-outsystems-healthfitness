package com.outsystems.plugins.healthfitness.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.outsystems.plugins.healthfitness.background.VariableUpdateManager
import com.outsystems.plugins.healthfitness.store.HealthFitnessManager
import com.outsystems.plugins.healthfitness.store.HealthStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
        val healthManager = HealthFitnessManager(context)
        val database = DatabaseManager.getInstance(context)
        val heathStore = HealthStore(context.applicationContext.packageName, healthManager, database)

        val vum = VariableUpdateManager(variableName, database, heathStore)
        vum.processBackgroundJobs(onSendNotification = { notification ->
            val title = notification.title
            val body = notification.body
            val id = notification.notificationID
            sendNotification(context, title, body, id)
        })
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
        val pendingIntent = PendingIntent.getActivity(context, 1, myIntent, PendingIntent.FLAG_UPDATE_CURRENT or 33554432)

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