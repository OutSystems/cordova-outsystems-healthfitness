package com.outsystems.plugins.healthfitness.background.database

interface DatabaseManagerInterface {
    fun insert(backgroundJob: BackgroundJob) : Long?
    fun insert(notification: Notification) : Long?
    fun fetchNotifications() : List<Notification>?
    fun fetchBackgroundJobs(variable : String) : List<BackgroundJob>?
    fun fetchBackgroundJob(id: String) : BackgroundJob?
    fun fetchBackgroundJobCountForVariable(variable: String) : Int
    fun fetchBackgroundJobs() : List<BackgroundJob>?
    fun fetchNotification(id : Long) : Notification?
    fun deleteBackgroundJob(backgroundJob: BackgroundJob)
    fun updateBackgroundJob(backgroundJob: BackgroundJob)
    fun updateNotification(notification: Notification)
    fun runInTransaction(closude : () -> Unit)
}