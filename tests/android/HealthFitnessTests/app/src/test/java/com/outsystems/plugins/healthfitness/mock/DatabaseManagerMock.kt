package com.outsystems.plugins.healthfitness.mock

import android.database.sqlite.SQLiteException
import com.outsystems.plugins.healthfitnesslib.background.database.BackgroundJob
import com.outsystems.plugins.healthfitnesslib.background.database.DatabaseManagerInterface
import com.outsystems.plugins.healthfitnesslib.background.database.Notification
import java.sql.SQLException

class DatabaseManagerMock: DatabaseManagerInterface {

    var backgroundJobAlreadyExists : Boolean = false

    override fun deleteBackgroundJob(backgroundJob: BackgroundJob) {

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


}