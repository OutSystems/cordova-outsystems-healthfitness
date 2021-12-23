package com.outsystems.plugins.healthfitness.background.database

import androidx.room.*

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

    @Update
    fun update(notification: Notification)

}