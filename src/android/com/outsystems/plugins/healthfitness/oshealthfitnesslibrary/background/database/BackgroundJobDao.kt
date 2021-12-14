package com.outsystems.plugins.healthfitnesslib.background.database

import androidx.room.*

@Dao
interface BackgroundJobDao {

    @Query("SELECT * FROM ${BackgroundJob.TABLE_NAME}")
    fun getAll(): List<BackgroundJob>

    @Query("SELECT COUNT(*) FROM ${BackgroundJob.TABLE_NAME} WHERE variable = :name")
    fun getBackgroundJobCountForVariable(name : String): Int

    @Query("SELECT * FROM ${BackgroundJob.TABLE_NAME} WHERE id = :id")
    fun findById(id: String): BackgroundJob?

    @Query("SELECT * FROM ${BackgroundJob.TABLE_NAME} WHERE variable = :name")
    fun findByVariableName(name : String): List<BackgroundJob>

    @Insert
    fun insert(backgroundJob: BackgroundJob): Long

    @Delete
    fun delete(backgroundJob: BackgroundJob)

    @Update
    fun update(backgroundJob: BackgroundJob)

}