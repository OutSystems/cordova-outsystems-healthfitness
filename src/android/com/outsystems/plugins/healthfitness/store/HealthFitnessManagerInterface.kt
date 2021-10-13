package com.outsystems.plugins.healthfitness.store

import android.app.Activity
import android.content.Context
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.result.DataReadResponse
import java.lang.Exception

interface HealthFitnessManagerInterface {

    fun createAccount(options: FitnessOptions)
    fun areGoogleFitPermissionsGranted(options: FitnessOptions?): Boolean
    fun requestPermissions(fitnessOptions: FitnessOptions)
    fun updateDataOnStore(dataSet: DataSet?, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun getDataFromStore(queryInformation: AdvancedQuery, onSuccess: (DataReadResponse) -> Unit, onFailure: (Exception) -> Unit)

}