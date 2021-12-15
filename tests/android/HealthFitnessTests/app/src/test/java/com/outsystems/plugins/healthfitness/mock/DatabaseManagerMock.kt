package com.outsystems.plugins.healthfitness.mock

import android.database.sqlite.SQLiteException
import com.outsystems.plugins.healthfitnesslib.background.database.BackgroundJob
import com.outsystems.plugins.healthfitnesslib.background.database.DatabaseManagerInterface
import com.outsystems.plugins.healthfitnesslib.background.database.Notification
import java.lang.Exception
import java.sql.SQLException

class DatabaseManagerMock: DatabaseManagerInterface {

    var backgroundJobAlreadyExists : Boolean = false
    var databaseHasError : Boolean = false
    var hasBackgroundJobs : Boolean = false

    override fun deleteBackgroundJob(backgroundJob: BackgroundJob) {

    }

    override fun fetchBackgroundJob(id: String): BackgroundJob? {
        TODO("Not yet implemented")
    }

    override fun fetchBackgroundJobCountForVariable(variable: String): Int {
        TODO("Not yet implemented")
    }

    override fun fetchBackgroundJobs(): List<BackgroundJob>? {
        if(databaseHasError){
            val e = Exception()
            throw e
        }
        else{
            if(hasBackgroundJobs){
                val job = BackgroundJob()
                job.variable = "HEART_RATE"
                job.value = 80F
                job.comparison = "HIGHER"
                job.isActive = true
                job.notificationId = 12345
                job.nextNotificationTimestamp = 0
                job.notificationFrequency = "DAY"
                job.notificationFrequencyGrouping = 1
                job.timeUnit = "DAY"
                job.timeUnitGrouping = 1
                return arrayListOf(job)
            }
            else{
                return arrayListOf()
            }
        }
    }

    override fun fetchBackgroundJobs(variable: String): List<BackgroundJob>? {
        return arrayListOf()
    }

    override fun fetchNotification(id: Long): Notification? {
        return null
    }

    override fun fetchNotifications(): List<Notification>? {
        return arrayListOf()
    }

    override fun insert(backgroundJob: BackgroundJob): Long? {
        if(backgroundJobAlreadyExists) {
            throw SQLiteException()
        }
        return null
    }

    override fun insert(notification: Notification): Long? {
        return null
    }

    override fun runInTransaction(closude: () -> Unit) {
        closude()
    }

    override fun updateBackgroundJob(backgroundJob: BackgroundJob) {
        TODO("Not yet implemented")
    }

    override fun updateNotification(notification: Notification) {
        TODO("Not yet implemented")
    }


}