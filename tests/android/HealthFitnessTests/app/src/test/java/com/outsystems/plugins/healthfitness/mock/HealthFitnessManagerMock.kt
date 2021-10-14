package com.outsystems.plugins.healthfitness.mock

import android.app.Activity
import android.content.Intent
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.result.DataReadResponse
import com.outsystems.plugins.healthfitness.store.AdvancedQuery
import com.outsystems.plugins.healthfitness.store.HealthFitnessManagerInterface
import com.outsystems.plugins.healthfitness.store.HealthStore
import java.lang.Exception

class HealthFitnessManagerMock: HealthFitnessManagerInterface {

    var permissionsGranted : Boolean = true
    var updateDataSuccess : Boolean = true
    var getDataSuccess : Boolean = true
    var store : HealthStore? = null

    override fun createAccount(options: FitnessOptions) {
        //Does nothing
    }

    override fun areGoogleFitPermissionsGranted(options: FitnessOptions?): Boolean {
        return permissionsGranted
    }

    override fun requestPermissions(fitnessOptions: FitnessOptions, resultCode : Int) {
        if(permissionsGranted){
            store?.handleActivityResult(Activity.RESULT_OK, resultCode, Intent())
        }
        else {
            store?.handleActivityResult(Activity.RESULT_CANCELED, resultCode, Intent())
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

}