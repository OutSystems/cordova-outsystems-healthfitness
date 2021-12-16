package com.outsystems.plugins.healthfitness.mock

import android.content.Intent
import com.outsystems.plugins.healthfitness.HealthFitnessError
import com.outsystems.plugins.healthfitness.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitness.background.BackgroundJobsResponse
import com.outsystems.plugins.healthfitness.background.UpdateBackgroundJobParameters
import com.outsystems.plugins.healthfitness.store.AdvancedQueryParameters
import com.outsystems.plugins.healthfitness.store.AdvancedQueryResponse
import com.outsystems.plugins.healthfitness.store.GoogleFitVariable
import com.outsystems.plugins.healthfitness.store.HealthStoreInterface

class HealthStoreMock: HealthStoreInterface {

    val advancedQuerySuccess: Boolean = true
    var advancedQueryResponseForVariable: Map<String, AdvancedQueryResponse> = mapOf()

    override fun getVariableByName(name : String) : GoogleFitVariable? {
        return null
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, intent: Intent): String? {
        TODO("Not yet implemented")
    }

    override fun initAndRequestPermissions(
        customPermissions: String,
        allVariables: String,
        fitnessVariables: String,
        healthVariables: String,
        profileVariables: String,
        summaryVariables: String
    ) {
        TODO("Not yet implemented")
    }

    override fun listBackgroundJobs(
        onSuccess: (BackgroundJobsResponse) -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun requestGoogleFitPermissions(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setBackgroundJob(
        parameters: BackgroundJobParameters,
        onSuccess: (String) -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun updateBackgroundJob(
        parameters: UpdateBackgroundJobParameters,
        onSuccess: (String) -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun updateDataAsync(
        variableName: String,
        value: Float,
        onSuccess: (String) -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun advancedQueryAsync(parameters : AdvancedQueryParameters,
                           onSuccess : (AdvancedQueryResponse) -> Unit,
                           onError : (HealthFitnessError) -> Unit) {

        if(advancedQuerySuccess &&
            advancedQueryResponseForVariable.containsKey(parameters.variable)){
            onSuccess(advancedQueryResponseForVariable[parameters.variable]!!)
        }
    }

    override fun areGoogleFitPermissionsGranted(): Boolean {
        TODO("Not yet implemented")
    }

    override fun deleteBackgroundJob(
        jogId: String,
        onSuccess: (String) -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getLastRecordAsync(
        variable: String,
        onSuccess: (AdvancedQueryResponse) -> Unit,
        onError: (HealthFitnessError) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}