package com.outsystems.plugins.healthfitnesslib.background.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
class Notification{

    // Should we use UUID for this?
    @PrimaryKey(name = "id", autoGenerate = true) var id: Long
    @ColumnInfo(name = "notification_id") var notificationID: Int
    @ColumnInfo(name = "title") var title: String = ""
    @ColumnInfo(name = "body") var body: String = ""

    init {
        id = 0
        notificationID = computeAutoIncrementId()
    }

    private fun computeAutoIncrementId() : Int {
        val random = Random()
        return random.nextInt()
    }

    companion object {
        const val TABLE_NAME: String= "notification"
    }
}