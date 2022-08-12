package com.outsystems.plugins.healthfitness.store

import android.R.attr.data
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessActivities
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataUpdateListenerRegistrationRequest
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.gms.fitness.request.SessionInsertRequest
import com.google.android.gms.fitness.request.SessionReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.android.gms.fitness.result.SessionReadResponse
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks.await
import com.outsystems.plugins.healthfitness.HealthFitnessError
import com.outsystems.plugins.healthfitness.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitness.background.VariableUpdateService
import java.util.*
import java.util.concurrent.TimeUnit


class HealthFitnessManager(var context : Context, var activity : Activity? = null): HealthFitnessManagerInterface {

    companion object {
        private const val SESSION_NAME = "HFSession"
        private const val SESSION_ID = "HFSessionIdentifier"
        private const val SESSION_DESCRIPTION = "HFSessionDescription"
    }

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

    override fun getDataFromSession(queryInformation: AdvancedQuery,
                           onSuccess: (SessionReadResponse) -> Unit,
                           onFailure: (Exception) -> Unit) {

        //create sleep entries
        val dataSource = DataSource.Builder()
            .setType(DataSource.TYPE_RAW)
            .setDataType(DataType.TYPE_SLEEP_SEGMENT)
            .setAppPackageName(context)
            // Optional but recommended for identifying the stream if you have multiple streams with the same dataType.
            .setStreamName("streamName")
            .build()

        val oneDay = (24 * 60 * 60 * 1000)
        val halfAnHour = (30 * 60 * 1000)
        val fiveHours = (5 * 60 * 60 * 1000)

        val dataPoints = listOf(
            DataPoint.builder(dataSource)
                .setTimeInterval(queryInformation.startDate.time, (queryInformation.startDate.time) + halfAnHour , TimeUnit.MILLISECONDS)
                .setField(Field.FIELD_SLEEP_SEGMENT_TYPE, SleepStages.SLEEP_LIGHT)
                .build(),
            DataPoint.builder(dataSource)
                .setTimeInterval(queryInformation.startDate.time + oneDay, (queryInformation.startDate.time) + oneDay + fiveHours, TimeUnit.MILLISECONDS)
                .setField(Field.FIELD_SLEEP_SEGMENT_TYPE, SleepStages.SLEEP_DEEP)
                .build(),
            DataPoint.builder(dataSource)
                .setTimeInterval(queryInformation.startDate.time + oneDay + oneDay , queryInformation.startDate.time + oneDay + oneDay + halfAnHour + fiveHours, TimeUnit.MILLISECONDS)
                .setField(Field.FIELD_SLEEP_SEGMENT_TYPE, SleepStages.SLEEP_REM)
                .build(),
            DataPoint.builder(dataSource)
                .setTimeInterval(queryInformation.startDate.time + oneDay + oneDay, queryInformation.startDate.time + oneDay + oneDay + halfAnHour + fiveHours, TimeUnit.MILLISECONDS)
                .setField(Field.FIELD_SLEEP_SEGMENT_TYPE, SleepStages.AWAKE)
                .build()
        )
        val dataSet = DataSet.builder(dataSource)
            .addAll(dataPoints)
            .build()
        // Create the sleep session
        val session= Session.Builder()
            .setName("wadwd")
            .setIdentifier("dawdawd")
            .setDescription("awdawd")
            .setStartTime(queryInformation.startDate.getTime(), TimeUnit.MILLISECONDS)
            .setEndTime(queryInformation.endDate.getTime(), TimeUnit.MILLISECONDS)
            .setActivity(FitnessActivities.SLEEP)
            .build()
        // Build the request to insert the session.
        val request = SessionInsertRequest.Builder()
            .setSession(session)
            .addDataSet(dataSet)
            .build()
        // Insert the session into Fit platform
        Log.i("INSERT", "Inserting the session with the SessionsClient")
        Fitness.getSessionsClient(context, getLastAccount())
            .insertSession(request)
            .addOnSuccessListener {
                Log.i("INSERT","Session insert was successful!")
            }
            .addOnFailureListener { e ->
                Log.w("INSERT", "There was a problem inserting the session", e)
            }


        try {
            val session: Session = Session.Builder()
                //.setName(SESSION_NAME)
                //.setIdentifier(SESSION_ID)
                //.setDescription(SESSION_DESCRIPTION)
                .setStartTime(queryInformation.startDate.time, TimeUnit.MILLISECONDS)
                .build()
            val sessionClient = Fitness.getSessionsClient(context, getLastAccount())
            sessionClient.startSession(session)

            val request = SessionReadRequest.Builder()
                .readSessionsFromAllApps()
                // By default, only activity sessions are included, so it is necessary to explicitly
                // request sleep sessions. This will cause activity sessions to be *excluded*.
                .includeSleepSessions()
                // Sleep segment data is required for details of the fine-granularity sleep, if it is present.
                .read(DataType.TYPE_SLEEP_SEGMENT)
                .setTimeInterval(queryInformation.startDate.time, queryInformation.endDate.time, TimeUnit.MILLISECONDS)
                .build()

            sessionClient.readSession(request)
                .addOnSuccessListener{ response ->
                    onSuccess(response)
                }
                .addOnFailureListener{ error ->
                    onFailure(error)
                }

        } catch (e: Exception) {
            Log.i("error", "Sleep error ${e.message}")
        }

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
                                          variableName : String,
                                          grouping: Long,
                                          jobFrequency: TimeUnit,
                                          parameters: BackgroundJobParameters,
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
                getSubscritionPendingIntent(variableName)
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    override fun subscribeToHistoryUpdates(variable: GoogleFitVariable,
                                           variableName : String,
                                           onSuccess: () -> Unit,
                                           onFailure: (Exception) -> Unit) {

        val account = getLastAccount()
        if(account == null){
            onFailure(HealthStoreException(HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR))
            return
        }

        val request = DataUpdateListenerRegistrationRequest.Builder()
            .setDataType(variable.dataType)
            .setPendingIntent(getSubscritionPendingIntent(variableName))
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

    override fun unsubscribeFromAllUpdates(variable: GoogleFitVariable,
                                           variableName : String,
                                           onSuccess: () -> Unit,
                                           onFailure: (Exception) -> Unit) {

        val account = getLastAccount()
        if(account == null){
            onFailure(HealthStoreException(HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR))
            return
        }

        val request = getSubscritionPendingIntent(variableName)
        val dataSource = DataSource.Builder()
            .setDataType(variable.dataType)
            .setType(DataSource.TYPE_RAW)
            .build()

        var success = true
        await(Fitness.getRecordingClient(context, account)
            .unsubscribe(dataSource)
            .addOnFailureListener { success = false })

        await(Fitness.getSensorsClient(context, account)
            .remove(request)
            .addOnFailureListener { success = false })

        await(Fitness.getHistoryClient(context, account)
            .unregisterDataUpdateListener(request)
            .addOnFailureListener { success = false })

        if(success){
            onSuccess()
        }
        else {
            onFailure(HealthStoreException(HealthFitnessError.BACKGROUND_JOB_GENERIC_ERROR))
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

    private fun getSubscritionPendingIntent(variableName : String) : PendingIntent {
        //do the actual subscription to the variable updates
        //when MABS 7 stops being supported, we can use PendingIntent.FLAG_MUTABLE instead of 33554432
        val intent = Intent(context, VariableUpdateService::class.java)
        intent.putExtra(VariableUpdateService.VARIABLE_NAME, variableName)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getBroadcast(context, variableName.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or 33554432)
    }
}