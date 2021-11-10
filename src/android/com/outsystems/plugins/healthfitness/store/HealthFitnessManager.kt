package com.outsystems.plugins.healthfitnesslib.store

import android.app.Activity
import android.app.PendingIntent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import android.content.Context
import android.util.Log
import androidx.room.Room
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.request.DataUpdateListenerRegistrationRequest
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.outsystems.plugins.healthfitness.HealthFitnessError
import com.outsystems.plugins.healthfitnesslib.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitnesslib.background.DatabaseManager
import com.outsystems.plugins.healthfitnesslib.background.database.AppDatabase
import com.outsystems.plugins.healthfitnesslib.background.database.BackgroundJob
import com.outsystems.plugins.healthfitnesslib.background.database.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.util.concurrent.TimeUnit

class HealthFitnessManager(var context : Context, var activity : Activity? = null): HealthFitnessManagerInterface {

    override fun createAccount(options: FitnessOptions){
        GoogleSignIn.getAccountForExtension(context, options)
    }

    override fun areGoogleFitPermissionsGranted(options: FitnessOptions?): Boolean {
        return areGoogleFitPermissionsGranted(getLastAccount(), options)
    }

    override fun requestPermissions(fitnessOptions: FitnessOptions, resultCode : Int){
        if(activity == null) return
        GoogleSignIn.requestPermissions(
            activity,
            resultCode,
            getLastAccount(),
            fitnessOptions
        )
    }

    override fun updateDataOnStore(dataSet: DataSet?, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        Fitness.getHistoryClient(
            activity,
            getLastAccount()
        )
            .insertData(dataSet)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    override fun getDataFromStore(queryInformation: AdvancedQuery, onSuccess: (DataReadResponse) -> Unit, onFailure: (Exception) -> Unit){
        val fitnessRequest = queryInformation.getDataReadRequest()
        Fitness.getHistoryClient(context, getLastAccount())
            .readData(fitnessRequest)
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
    }

    override fun subscribeToRecordingUpdates(variable : GoogleFitVariable,
                                             parameters: BackgroundJobParameters,
                                             onSuccess: () -> Unit,
                                             onFailure: (Exception) -> Unit) {

        val account = getLastAccount()
        if(account == null){
            onFailure(HealthStoreException(HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR))
            return
        }

        val dataSource = DataSource.Builder()
            .setDataType(variable.dataType)
            .setType(DataSource.TYPE_RAW)
            .build()

        Fitness.getRecordingClient(context, account)
            .subscribe(dataSource)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener{
                onFailure(it)
            }
    }

    override fun subscribeToSensorUpdates(variable: GoogleFitVariable,
                                          grouping: Long,
                                          jobFrequency: TimeUnit,
                                          parameters: BackgroundJobParameters,
                                          pendingIntent : PendingIntent,
                                          onSuccess: () -> Unit,
                                          onFailure: (Exception) -> Unit) {

        val account = getLastAccount()
        if(account == null){
            onFailure(HealthStoreException(HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR))
            return
        }

        Fitness.getSensorsClient(context, account)
            .add(
                SensorRequest.Builder()
                    .setDataType(variable.dataType)
                    .setSamplingRate(grouping, jobFrequency)
                    .build(),
                pendingIntent
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    override fun subscribeToHistoryUpdates(variable: GoogleFitVariable,
                                           pendingIntent : PendingIntent,
                                           onSuccess: () -> Unit,
                                           onFailure: (Exception) -> Unit) {

        val account = getLastAccount()
        if(account == null){
            onFailure(HealthStoreException(HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR))
            return
        }

        val request = DataUpdateListenerRegistrationRequest.Builder()
            .setDataType(variable.dataType)
            .setPendingIntent(pendingIntent)
            .build()

        Fitness.getHistoryClient(context, account)
            .registerDataUpdateListener(request)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    private fun areGoogleFitPermissionsGranted(account : GoogleSignInAccount?, options: FitnessOptions?): Boolean {
        account.let {
            options.let {
                return GoogleSignIn.hasPermissions(account, options)
            }
        }
    }

    private fun getLastAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

}