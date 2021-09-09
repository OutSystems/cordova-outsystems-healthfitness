package com.outsystems.plugins.healthfitness

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.*
import android.os.Process.THREAD_PRIORITY_BACKGROUND

import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.fitness.data.DataUpdateNotification
import com.outsystems.rd.HealthFitnessSampleAppPOC.R
import java.util.concurrent.TimeUnit


class MyDataUpdateService : IntentService("MyDataUpdateService") {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onHandleIntent(intent: Intent?) {
        val update = DataUpdateNotification.getDataUpdateNotification(intent)
        // Show the time interval over which the data points were collected.
        // To extract specific data values, in this case the user's weight,
        // use DataReadRequest.
        Log.println(Log.INFO, "Health", "Data Update start1")



        NotificationManagerCompat.from(applicationContext).createNotificationChannel(
            NotificationChannel(
                "com.outsystems.health",
                "Notificaciones Healt",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        val notif = NotificationCompat.Builder(applicationContext, "com.outsystems.health")
            .setContentTitle("Titulo")
            //TODO: USE APP ICON
//            .setSma<llIcon(R.drawable.ic_stat_name)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(12,notif)

        //QUERY FOR COMPARE

        val compare = intent?.getBundleExtra("AMOUNTTOCPMARE").toString()



        update?.apply {
            val start = getUpdateStartTime(TimeUnit.MILLISECONDS)
            val end = getUpdateEndTime(TimeUnit.MILLISECONDS)
            Toast.makeText(applicationContext, "UPDATE TYPE_STEP_COUNT_DELTA", Toast.LENGTH_SHORT)
                .show()
            Log.println(
                Log.INFO,
                "Health",
                "Data Update start: $start end: $end DataType: ${dataType.name}"
            )

            Log.println(
                Log.INFO,
                "Healthupdate",
                update.toString()
            )
        }
    }
}