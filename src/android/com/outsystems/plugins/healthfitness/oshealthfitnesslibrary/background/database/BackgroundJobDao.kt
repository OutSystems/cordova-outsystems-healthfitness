package com.outsystems.plugins.healthfitnesslib.background.database

import androidx.room.*

@Dao
interface BackgroundJobDao {

    @Query("SELECT * FROM backgroundJob")
    fun getAll(): List<BackgroundJob>

    //@Query("SELECT * FROM backgroundJob WHERE variable = :variable AND comparison = :comparison AND value = :value")
    //fun findByPrimaryKey(variable: String, comparison: String, value: Float)

    @Query("SELECT * FROM backgroundJob WHERE variable = :name")
    fun findByVariableName(name : String): List<BackgroundJob>

    @Insert
    fun insert(backgroundJob: BackgroundJob) : Long

    @Delete
    fun delete(backgroundJob: BackgroundJob)

    @Update
    fun update(backgroundJob: BackgroundJob)

}