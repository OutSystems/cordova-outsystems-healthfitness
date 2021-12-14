package com.outsystems.plugins.healthfitnesslib.background.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import java.util.*

@Database(
    entities = [BackgroundJob::class, Notification::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun backgroundJobDao(): BackgroundJobDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val oldTable = "backgroundJob"
                val notificationFrequency = "ALWAYS"
                val notificationFrequencyGrouping = 1
                val nextNotificationAt = 0
                val isActive = 1

                database.execSQL(
                    "CREATE TABLE ${BackgroundJob.TABLE_NAME}( " +
                            "id TEXT NOT NULL PRIMARY KEY, " +
                            "variable TEXT NOT NULL, " +
                            "comparison TEXT NOT NULL, " +
                            "value REAL NOT NULL, " +
                            "time_unit TEXT, " +
                            "time_unit_grouping INTEGER, " +
                            "notification_id INTEGER, " +
                            "notification_frequency TEXT NOT NULL DEFAULT '$notificationFrequency', " +
                            "notification_frequency_grouping INTEGER NOT NULL DEFAULT $notificationFrequencyGrouping, " +
                            "next_notification_timestamp INTEGER NOT NULL DEFAULT $nextNotificationAt, " +
                            "isActive INTEGER NOT NULL DEFAULT $isActive, " +
                            "UNIQUE(variable, comparison, value), " +
                            "FOREIGN KEY(notification_id) REFERENCES Notification(id) ON DELETE CASCADE" +
                            ");")

                database.execSQL(
                    "CREATE UNIQUE INDEX unique_bg_job " +
                            "ON ${BackgroundJob.TABLE_NAME}(variable, comparison, value);")

                val jobs = database.query(
                    "SELECT variable,comparison,value,time_unit,time_unit_grouping,notification_id " +
                            "FROM $oldTable;")

                for(i in 0 until jobs.count){
                    jobs.moveToPosition(i)
                    val uuid = UUID.randomUUID().toString()
                    val variable = jobs.getString(0)
                    val comparison = jobs.getString(1)
                    val value = jobs.getFloat(2)
                    val timeUnit = jobs.getString(3)
                    val timeUnitGrouping = jobs.getInt(4)
                    val notificationId = jobs.getInt(5)

                    database.execSQL(
                        "INSERT INTO ${BackgroundJob.TABLE_NAME} " +
                                "(id," +
                                "variable," +
                                "comparison," +
                                "value," +
                                "time_unit," +
                                "time_unit_grouping," +
                                "notification_id," +
                                "notification_frequency," +
                                "notification_frequency_grouping," +
                                "next_notification_timestamp," +
                                "isActive) " +
                                "VALUES (" +
                                "'$uuid'," +
                                "'$variable'," +
                                "'$comparison'," +
                                "$value," +
                                "'$timeUnit'," +
                                "$timeUnitGrouping," +
                                "$notificationId," +
                                "'$notificationFrequency'," +
                                "$notificationFrequencyGrouping," +
                                "$nextNotificationAt," +
                                "$isActive" +
                                ");"
                    )
                    database.execSQL("DROP TABLE IF EXISTS $oldTable;")
                }
            }
        }
    }
}