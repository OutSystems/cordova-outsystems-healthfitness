package com.outsystems.plugins.healthfitnesslib.background.database

import androidx.room.*
import androidx.room.ColumnInfo

@Entity(
    tableName = BackgroundJob.TABLE_NAME,
    primaryKeys = ["variable", "comparison", "value"],
    foreignKeys = [ForeignKey(
        entity = Notification::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("notification_id"),
        onDelete = ForeignKey.CASCADE)]
)
open class BackgroundJob {

    enum class ComparisonOperationEnum(val id : String) {
        GREATER("HIGHER"),
        GREATER_OR_EQUALS("HIGHER_EQUALS"),
        LESSER("LOWER"),
        LESSER_OR_EQUALS("LOWER_EQUALS"),
        EQUALS("EQUALS"),
    }

    @ColumnInfo(name = "variable") var variable: String = ""
        set(value){
            field = value
            computeId()
        }
    @ColumnInfo(name = "comparison") var comparison: String = ""
        set(value){
            field = value
            computeId()
        }
    @ColumnInfo(name = "value") var value: Float = 0.0f
        set(value){
            field = value
            computeId()
        }

    @ColumnInfo(name = "time_unit") var timeUnit: String? = null
    @ColumnInfo(name = "time_unit_grouping") var timeUnitGrouping: Int? = null
    @ColumnInfo(name = "notification_id") var notificationId: Long? = null
    @ColumnInfo(name = "notification_frequency") var notificationFrequency: String = "ALWAYS"
    @ColumnInfo(name = "notification_frequency_grouping") var notificationFrequencyGrouping: Int = 1
    @ColumnInfo(name = "next_notification_timestamp") var nextNotificationTimestamp: Long = 0
    @ColumnInfo(name = "active") var active: Boolean = true

    @Ignore
    var id: String = ""

    private fun computeId() {
        id = "${this.variable}-${this.comparison}-${this.value}".toLowerCase()
    }

    companion object {
        const val TABLE_NAME: String= "backgroundJob"
    }
}