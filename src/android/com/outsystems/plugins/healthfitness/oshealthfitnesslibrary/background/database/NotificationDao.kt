package com.outsystems.plugins.healthfitnesslib.background.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotificationDao {

    @Query("SELECT * FROM ${Notification.TABLE_NAME}")
    fun getAll(): List<Notification>

    @Query("SELECT * FROM ${Notification.TABLE_NAME} WHERE id = :id")
    fun findById(id : Long): List<Notification>

    @Insert
    fun insert(notification: Notification) : Long

    @Delete
    fun delete(notification: Notification)

}