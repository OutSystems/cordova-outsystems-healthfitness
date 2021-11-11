package com.outsystems.plugins.healthfitnesslib.background

import android.content.Context
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Room
import com.outsystems.plugins.healthfitnesslib.background.database.*
import kotlinx.coroutines.Runnable

class DatabaseManager(context : Context) {

    private var database : AppDatabase? = null
    private var backgroundJobDao : BackgroundJobDao? = null
    private var notificationDao : NotificationDao? = null

    init {
        try {
            // Should we close the database instance? If so, when.
            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "database-name"
            ).build()

            backgroundJobDao = database!!.backgroundJobDao()
            notificationDao = database!!.notificationDao()
        }
        catch (e : Exception){
            Log.e("Room database creation:" , e.printStackTrace().toString())
        }
    }

    companion object {
        var instance: DatabaseManager? = null
        fun getInstance(context: Context): DatabaseManager {
            if (instance == null) {
                instance = DatabaseManager(context)
            }
            return instance!!
        }
    }

    fun insert(backgroundJob: BackgroundJob) : Long? {
        return backgroundJobDao?.insert(backgroundJob)
    }

    fun insert(notification: Notification) : Long? {
        return notificationDao?.insert(notification)
    }

    fun fetchNotifications() : List<Notification>? {
        return notificationDao?.getAll()
    }

    fun fetchBackgroundJobs(variable : String) : List<BackgroundJob>? {
        return backgroundJobDao?.findByVariableName(variable)
    }

    //fun fetchBackgroundJob(variable: String, comparison: String, value: Float) {
    //    return backgroundJobDao?.findByPrimaryKey(variable, comparison, value)
    //}

    fun fetchNotification(id : Long) : Notification? {
        return notificationDao?.findById(id)?.first()
    }

    fun deleteBackgroundJob(backgroundJob : BackgroundJob) {
        backgroundJobDao?.delete(backgroundJob)
    }

    fun runInTransaction(closude : () -> Unit){
        database?.runInTransaction(Runnable {
            closude()
        });
    }

}