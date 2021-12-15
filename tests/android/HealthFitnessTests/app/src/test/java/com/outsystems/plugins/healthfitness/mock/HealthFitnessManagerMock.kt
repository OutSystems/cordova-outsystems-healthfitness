package com.outsystems.plugins.healthfitness.mock

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.result.DataReadResponse
import com.outsystems.plugins.healthfitnesslib.HealthFitnessError
import com.outsystems.plugins.healthfitnesslib.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitnesslib.store.*
import java.lang.Exception
import java.util.concurrent.TimeUnit

class HealthFitnessManagerMock: HealthFitnessManagerInterface {

    var permissionsGranted : Boolean = true
    var permissionsGrantedOnRequest : Boolean = true
    var backgroundJobSuccess : Boolean = true
    var updateDataSuccess : Boolean = true
    var getDataSuccess : Boolean = true
    var store : HealthStore? = null
    var unsubscribeError : Boolean = false

    override fun createAccount(options: FitnessOptions) {
        //Does nothing
    }

    override fun areGoogleFitPermissionsGranted(options: FitnessOptions?): Boolean {
        return permissionsGranted
    }

    override fun requestPermissions(fitnessOptions: FitnessOptions, requestCode : Int) {
        if(permissionsGrantedOnRequest){
            store?.handleActivityResult(requestCode, Activity.RESULT_OK, Intent())
        }
        else {
            store?.handleActivityResult(requestCode, Activity.RESULT_CANCELED, Intent())
        }
    }

    override fun updateDataOnStore(dataSet: DataSet?, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if(updateDataSuccess){
            onSuccess()
        }
        else{
            onFailure(Exception())
        }
    }

    override fun getDataFromStore(queryInformation: AdvancedQuery, onSuccess: (DataReadResponse) -> Unit, onFailure: (Exception) -> Unit) {
        if(getDataSuccess){
            onSuccess(DataReadResponse())
        }
        else{
            onFailure(Exception())
        }
    }

    override fun subscribeToHistoryUpdates(
        variable: GoogleFitVariable,
        variableName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if(backgroundJobSuccess){
            onSuccess()
        }
        else {
            onFailure(Exception())
        }
    }

    override fun subscribeToRecordingUpdates(
        variable: GoogleFitVariable,
        parameters: BackgroundJobParameters,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if(backgroundJobSuccess){
            onSuccess()
        }
        else {
            onFailure(Exception())
        }
    }

    override fun subscribeToSensorUpdates(
        variable: GoogleFitVariable,
        variableName: String,
        grouping: Long,
        jobFrequency: TimeUnit,
        parameters: BackgroundJobParameters,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if(backgroundJobSuccess){
            onSuccess()
        }
        else {
            onFailure(Exception())
        }
    }

    override fun unsubscribeFromAllUpdates(
        variable: GoogleFitVariable,
        variableName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if(unsubscribeError){
            onFailure(HealthStoreException(HealthFitnessError.BACKGROUND_JOB_GENERIC_ERROR))
        }
        else{
            onSuccess()
        }
    }

}