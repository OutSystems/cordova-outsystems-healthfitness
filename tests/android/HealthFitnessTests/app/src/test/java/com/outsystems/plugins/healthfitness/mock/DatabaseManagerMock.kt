package com.outsystems.plugins.healthfitness.mock

import android.database.sqlite.SQLiteException
import com.outsystems.plugins.healthfitness.background.database.BackgroundJob
import com.outsystems.plugins.healthfitness.background.database.DatabaseManagerInterface
import com.outsystems.plugins.healthfitness.background.database.Notification

class DatabaseManagerMock: DatabaseManagerInterface {

    var backgroundJobExists : Boolean = false
    var databaseHasError : Boolean = false
    var hasBackgroundJobs : Boolean = false
    var backgroundJobs: MutableList<BackgroundJob> = mutableListOf()

    override fun deleteBackgroundJob(backgroundJob: BackgroundJob) {
        if(databaseHasError){
            throw SQLiteException()
        }
    }

    override fun fetchBackgroundJob(id: String): BackgroundJob? {
        if(backgroundJobExists){
            val job = BackgroundJob()
            job.notificationId = 1234
            return job
        }
        else{
            return null
        }
    }

    override fun fetchBackgroundJobCountForVariable(variable: String): Int {
        TODO("Not yet implemented")
    }

    override fun fetchBackgroundJobs(): List<BackgroundJob>? {
        if(databaseHasError){
            throw SQLiteException()
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
        return backgroundJobs
    }

    override fun fetchNotification(id: Long): Notification? {
        if(backgroundJobExists){
            return Notification()
        }
        else{
            return null
        }
    }

    override fun fetchNotifications(): List<Notification>? {
        return arrayListOf()
    }

    override fun insert(backgroundJob: BackgroundJob): Long? {
        if(backgroundJobExists) {
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
        backgroundJobs.forEach { dbJob ->
            if(dbJob.equals(backgroundJob)){
                dbJob.nextNotificationTimestamp = backgroundJob.nextNotificationTimestamp
                return
            }
        }
        if(databaseHasError){
            throw SQLiteException()
        }
    }

    override fun updateNotification(notification: Notification) {
        if(backgroundJobExists){
            // do nothing
        }
    }

}