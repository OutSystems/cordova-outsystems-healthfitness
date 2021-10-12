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

    override fun createAccount(context: Context, options: FitnessOptions) {
        TODO("Not yet implemented")
    }

    override fun areGoogleFitPermissionsGranted(
        activity: Activity,
        options: FitnessOptions?
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun requestPermissions(activity: Activity, fitnessOptions: FitnessOptions) {
        TODO("Not yet implemented")
    }

    override fun updateDataOnStore(
        activity: Activity,
        dataSet: DataSet?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getDataFromStore(
        activity: Activity,
        queryInformation: AdvancedQuery,
        onSuccess: (DataReadResponse) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        TODO("Not yet implemented")
    }


}