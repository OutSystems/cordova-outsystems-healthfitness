package com.outsystems.plugins.healthfitnesslib.background.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BackgroundJob::class, Notification::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun backgroundJobDao(): BackgroundJobDao
    abstract fun notificationDao(): NotificationDao
}