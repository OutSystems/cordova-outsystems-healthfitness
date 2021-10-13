package com.outsystems.plugins.healthfitness

import android.app.Activity
import android.content.Context
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.result.DataReadResponse
import com.outsystems.plugins.healthfitness.store.AdvancedQuery
import com.outsystems.plugins.healthfitness.store.HealthFitnessManagerInterface
import java.lang.Exception

class HealthFitnessManagerMock: HealthFitnessManagerInterface {

    var permissionsGranted : Boolean = true

    override fun createAccount(options: FitnessOptions) {
        TODO("Not yet implemented")
    }

    override fun areGoogleFitPermissionsGranted(options: FitnessOptions?): Boolean {
        return permissionsGranted
    }

    override fun requestPermissions(fitnessOptions: FitnessOptions) {
        TODO("Not yet implemented")
    }

    override fun updateDataOnStore(dataSet: DataSet?, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getDataFromStore(queryInformation: AdvancedQuery, onSuccess: (DataReadResponse) -> Unit, onFailure: (Exception) -> Unit) {
        TODO("Not yet implemented")
    }

}