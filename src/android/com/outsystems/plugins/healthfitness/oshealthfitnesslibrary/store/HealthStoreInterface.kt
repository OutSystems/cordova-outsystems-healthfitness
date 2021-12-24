package com.outsystems.plugins.healthfitness.store

import android.content.Intent
import com.outsystems.plugins.healthfitness.HealthFitnessError
import com.outsystems.plugins.healthfitness.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitness.background.BackgroundJobsResponse
import com.outsystems.plugins.healthfitness.background.UpdateBackgroundJobParameters

interface HealthStoreInterface {

    fun getVariableByName(name : String) : GoogleFitVariable?
    fun initAndRequestPermissions(customPermissions: String,
                                  allVariables: String,
                                  fitnessVariables: String,
                                  healthVariables: String,
                                  profileVariables: String,
                                  summaryVariables: String)
    fun requestGoogleFitPermissions() : Boolean
    fun handleActivityResult(requestCode: Int,
                             resultCode: Int,
                             intent: Intent) : String?

    fun updateDataAsync(variableName: String,
                        value: Float,
                        onSuccess : (String) -> Unit,
                        onError : (HealthFitnessError) -> Unit)

    fun getLastRecordAsync(variable: String,
                           onSuccess : (AdvancedQueryResponse) -> Unit,
                           onError : (HealthFitnessError) -> Unit)

    fun areGoogleFitPermissionsGranted(): Boolean

    fun advancedQueryAsync(parameters : AdvancedQueryParameters,
                           onSuccess : (AdvancedQueryResponse) -> Unit,
                           onError : (HealthFitnessError) -> Unit)

    fun setBackgroundJob(parameters: BackgroundJobParameters,
                         onSuccess : (String) -> Unit,
                         onError : (HealthFitnessError) -> Unit)

    fun deleteBackgroundJob(jogId: String,
                            onSuccess : (String) -> Unit,
                            onError : (HealthFitnessError) -> Unit)

    fun listBackgroundJobs(onSuccess : (BackgroundJobsResponse) -> Unit,
                           onError: (HealthFitnessError) -> Unit)

    fun updateBackgroundJob(parameters: UpdateBackgroundJobParameters,
                            onSuccess: (String) -> Unit,
                            onError: (HealthFitnessError) -> Unit)
}