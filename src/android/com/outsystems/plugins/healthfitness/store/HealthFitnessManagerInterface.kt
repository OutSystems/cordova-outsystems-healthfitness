package com.outsystems.plugins.healthfitness.store

import android.app.Activity
import android.content.Context
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.result.DataReadResponse
import java.lang.Exception

interface HealthFitnessManagerInterface {

    fun createAccount(context: Context, options: FitnessOptions)
    fun areGoogleFitPermissionsGranted(options: FitnessOptions?): Boolean
    fun requestPermissions(activity: Activity, fitnessOptions: FitnessOptions)
    fun updateDataOnStore(activity: Activity, dataSet: DataSet?, onSuccess: (Void) -> Unit, onFailure: (Exception) -> Unit)
    fun getDataFromStore(activity: Activity, queryInformation: AdvancedQuery, onSuccess: (DataReadResponse) -> Unit, onFailure: (Exception) -> Unit)

}