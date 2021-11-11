package com.outsystems.plugins.healthfitnesslib.store

import android.app.PendingIntent
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.result.DataReadResponse
import com.outsystems.plugins.healthfitnesslib.background.BackgroundJobParameters
import java.lang.Exception
import java.util.concurrent.TimeUnit

interface HealthFitnessManagerInterface {

    fun createAccount(options: FitnessOptions)
    fun areGoogleFitPermissionsGranted(options: FitnessOptions?): Boolean
    fun requestPermissions(fitnessOptions: FitnessOptions, resultCode: Int)
    fun updateDataOnStore(dataSet: DataSet?, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun getDataFromStore(queryInformation: AdvancedQuery,
                         onSuccess: (DataReadResponse) -> Unit,
                         onFailure: (Exception) -> Unit)

    fun subscribeToRecordingUpdates(variable: GoogleFitVariable,
                                    parameters: BackgroundJobParameters,
                                    onSuccess: () -> Unit,
                                    onFailure: (Exception) -> Unit)

    fun subscribeToSensorUpdates(variable: GoogleFitVariable,
                                 grouping: Long,
                                 jobFrequency: TimeUnit,
                                 parameters: BackgroundJobParameters,
                                 pendingIntent: PendingIntent,
                                 onSuccess: () -> Unit,
                                 onFailure: (Exception) -> Unit)

    fun subscribeToHistoryUpdates(variable: GoogleFitVariable,
                                  pendingIntent : PendingIntent,
                                  onSuccess: () -> Unit,
                                  onFailure: (Exception) -> Unit)
}
