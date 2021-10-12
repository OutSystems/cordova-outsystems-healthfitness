package com.outsystems.plugins.healthfitness.store

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.outsystems.plugins.healthfitness.OSHealthFitness
import java.lang.Exception

class HealthFitnessManager: HealthFitnessManagerInterface {

    override fun createAccount(context: Context, options: FitnessOptions){
        GoogleSignIn.getAccountForExtension(context, options)
    }

    override fun areGoogleFitPermissionsGranted(activity: Activity, options: FitnessOptions?): Boolean {
        return areGoogleFitPermissionsGranted(getLastAccount(activity), options)
    }

    override fun requestPermissions(activity: Activity, fitnessOptions: FitnessOptions){
        GoogleSignIn.requestPermissions(
            activity,
            OSHealthFitness.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
            getLastAccount(activity),
            fitnessOptions
        )
    }

    override fun updateDataOnStore(activity: Activity, dataSet: DataSet?, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        Fitness.getHistoryClient(
            activity,
            getLastAccount(activity)
        )
            .insertData(dataSet)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    override fun getDataFromStore(activity: Activity, queryInformation: AdvancedQuery, onSuccess: (DataReadResponse) -> Unit, onFailure: (Exception) -> Unit){
        val fitnessRequest = queryInformation.getDataReadRequest()
        Fitness.getHistoryClient(activity, getLastAccount(activity))
            .readData(fitnessRequest)
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
    }

    private fun areGoogleFitPermissionsGranted(account : GoogleSignInAccount?, options: FitnessOptions?): Boolean {
        account.let {
            options.let {
                return GoogleSignIn.hasPermissions(account, options)
            }
        }
    }

    private fun getLastAccount(activity: Activity): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(activity)
    }

}