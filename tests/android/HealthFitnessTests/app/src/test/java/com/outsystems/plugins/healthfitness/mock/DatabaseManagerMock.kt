package com.outsystems.plugins.healthfitness.mock

import com.outsystems.plugins.healthfitnesslib.background.database.BackgroundJob
import com.outsystems.plugins.healthfitnesslib.background.database.DatabaseManagerInterface
import com.outsystems.plugins.healthfitnesslib.background.database.Notification

class DatabaseManagerMock: DatabaseManagerInterface {
    override fun deleteBackgroundJob(backgroundJob: BackgroundJob) {
        TODO("Not yet implemented")
    }

    override fun fetchBackgroundJobs(variable: String): List<BackgroundJob>? {
        TODO("Not yet implemented")
    }

    override fun fetchNotification(id: Long): Notification? {
        TODO("Not yet implemented")
    }

    override fun fetchNotifications(): List<Notification>? {
        TODO("Not yet implemented")
    }

    override fun insert(backgroundJob: BackgroundJob): Long? {
        TODO("Not yet implemented")
    }

    override fun insert(notification: Notification): Long? {
        TODO("Not yet implemented")
    }

    override fun runInTransaction(closude: () -> Unit) {
        TODO("Not yet implemented")
    }


}