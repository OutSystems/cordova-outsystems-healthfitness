package com.outsystems.plugins.healthfitnesslib.background

import android.content.Context
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Room
import com.outsystems.plugins.healthfitnesslib.background.database.*
import kotlinx.coroutines.Runnable

class DatabaseManager(context : Context) : DatabaseManagerInterface {

    private var database : AppDatabase? = null
    private var backgroundJobDao : BackgroundJobDao? = null
    private var notificationDao : NotificationDao? = null

    init {
        try {
            // Should we close the database instance? If so, when.
            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "database-name")
                .addMigrations(AppDatabase.MIGRATION_1_2)
                .build()

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

    override fun insert(backgroundJob: BackgroundJob) : Long? {
        return backgroundJobDao?.insert(backgroundJob)
    }

    override fun insert(notification: Notification) : Long? {
        return notificationDao?.insert(notification)
    }

    override fun fetchNotifications() : List<Notification>? {
        return notificationDao?.getAll()
    }

    override fun fetchBackgroundJob(id: String) : BackgroundJob? {
        return backgroundJobDao?.findById(id)
    }

    override fun fetchBackgroundJobCountForVariable(variable: String) : Int {
        return backgroundJobDao?.getBackgroundJobCountForVariable(variable) ?: 0
    }

    override fun fetchBackgroundJobs(variable: String) : List<BackgroundJob>? {
        return backgroundJobDao?.findByVariableName(variable)
    }

    override fun fetchBackgroundJobs() : List<BackgroundJob>? {
        return backgroundJobDao?.getAll()
    }

    override fun fetchNotification(id : Long) : Notification? {
        return notificationDao?.findById(id)?.first()
    }

    override fun deleteBackgroundJob(backgroundJob: BackgroundJob) {
        backgroundJobDao?.delete(backgroundJob)
    }

    override fun updateBackgroundJob(backgroundJob: BackgroundJob) {
        backgroundJobDao?.update(backgroundJob)
    }

    override fun runInTransaction(closude : () -> Unit){
        database?.runInTransaction(Runnable {
            closude()
        });
    }

}