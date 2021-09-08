package com.outsystems.plugins.healthfitness

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.DataUpdateListenerRegistrationRequest
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.apache.cordova.*
import org.apache.cordova.PluginResult
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Error
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit


enum class EnumPermissionAccess {
    GRANTED,
    DENIED,
    FULLY_DENIED
}

class HealthFitness : CordovaPlugin() {
    private var fitnessOptions: FitnessOptions? = null
    private var account: GoogleSignInAccount? = null
    private var googleFitPermission: List<Pair<DataType, Int>> = listOf()

    private  var callbackContext: CallbackContext? = null

    override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
        super.initialize(cordova, webView)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Throws(JSONException::class)
    override fun execute(
            action: String,
            args: JSONArray,
            callbackContext: CallbackContext
    ): Boolean {
        this.callbackContext = callbackContext
        when (action) {
            "requestPermissions" -> {
                initAndRequestPermissions()
            }
            "getData" -> {
                getData()
            }
            "updateData" -> {
                updateData()
            }
            "enableBackgroundJob" -> {
                enableBackgroundJob()
            }
        }
        return false
    }

    fun <T> callBackResult(resultVariable: T, error: String? = null) {
        resultVariable?.let {
            val jsonResult = JSONObject()
            jsonResult.put("value", resultVariable)
            this.callbackContext?.let {
                it.success(jsonResult)
            }
            return
        }
        val jsonResult = JSONObject()
        jsonResult.put("ErrorCode", 404)
        jsonResult.put("ErrorMessage", error ?: "No Results")
        this.callbackContext?.let {
            it.error(jsonResult)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initAndRequestPermissions() {
        googleFitPermission = listOf(
                Pair(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ),
                Pair(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ),
                Pair(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_READ),
                Pair(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE),
                Pair(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_READ),
                Pair(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        )
        checkAndGrantPermissions()
    }

    private fun initFitnessOption(params: List<Pair<DataType, Int>>) {
        var fitnessBuild = FitnessOptions.builder()
        params.forEach {
            fitnessBuild.addDataType(it.first, it.second)
        }
        fitnessOptions = fitnessBuild.build()
        account = GoogleSignIn.getAccountForExtension(cordova.context, fitnessOptions!!)
    }

    private fun checkAllPermissionGranted(permissions: Array<String>): Boolean {
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(
                            cordova.activity,
                            it
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun checkAllGoogleFitPermissionGranted(): Boolean {
        account.let {
            fitnessOptions.let {
                return GoogleSignIn.hasPermissions(account!!, fitnessOptions!!)
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun checkAndGrantPermissions(): Boolean? {
        initFitnessOption(googleFitPermission)
        val permissions = arrayOf(
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BODY_SENSORS
        )
        if (checkAllPermissionGranted(permissions)) {
            if (!checkAllGoogleFitPermissionGranted()) {
                requestGoogleFitPermissions()
            } else {
                enableBackgroundJob()
                dumpSubscriptionsList()
                return true
            }
        } else {
            PermissionHelper.requestPermissions(
                    this,
                    ACTIVITY_LOCATION_PERMISSIONS_REQUEST_CODE,
                    permissions
            )
        }
        return false
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun requestGoogleFitPermissions() {
        fitnessOptions?.let {
            GoogleSignIn.requestPermissions(
                    cordova.activity,  // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,  // e.g. 1
                    account,
                    it
            )
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun getData() {
        val end = LocalDateTime.now()
        val start = end.minusYears(1L)
        val endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond()
        val startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond()
        val context = cordova.context
        val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_WEIGHT)
                .setTimeRange(startSeconds, endSeconds, TimeUnit.SECONDS)
                .setLimit(1)
                .build()
        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
        var resultVariable: Float? = null
        Fitness.getHistoryClient(context, account).readData(readRequest)
                .addOnSuccessListener { dataReadResponse: DataReadResponse ->
                    resultVariable = dataReadResponse.dataSets[0].dataPoints.firstOrNull()
                            ?.getValue(Field.FIELD_WEIGHT)?.asFloat()
                    Log.d(
                            "Access GoogleFit:",
                            dataReadResponse.dataSets[0].dataPoints.firstOrNull()
                                    ?.getValue(Field.FIELD_WEIGHT).toString()
                    )
                }
                .addOnFailureListener { dataReadResponse: Exception ->
                    Log.d(
                            "TAG",
                            dataReadResponse.message!!
                    )
                }
        callBackResult(resultVariable)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun updateData() {
        val weightSource = DataSource.Builder()
                .setAppPackageName(cordova.context)
                .setDataType(DataType.TYPE_WEIGHT)
                .setType(DataSource.TYPE_RAW)
                .build()

        val timestamp = System.currentTimeMillis()
        val weight = DataPoint.builder(weightSource)
                .setTimestamp(timestamp, TimeUnit.MILLISECONDS)
                .setField(Field.FIELD_WEIGHT, 90f)
                .build()

        val dataSet = DataSet.builder(weightSource)
                .add(weight)
                .build()

        var updatedField: Boolean? = null

        Fitness.getHistoryClient(
                cordova.activity,
                GoogleSignIn.getAccountForExtension(cordova.context, fitnessOptions)
        )
                .insertData(dataSet)
                .addOnSuccessListener {
                    updatedField = true
                    Log.i("Access GoogleFit:", "DataSet updated successfully!")
                }
                .addOnFailureListener { e ->
                    Log.w("Access GoogleFit:", "There was an error updating the DataSet", e)
                }
        callBackResult(updatedField)
    }

    private fun dumpSubscriptionsList() {
        // [START list_current_subscriptions]
        Fitness.getRecordingClient(cordova.context, account)
                .listSubscriptions(DataType.TYPE_WEIGHT)
                .addOnSuccessListener { subscriptions ->
                    for (subscription in subscriptions) {
                        val dataType = subscription.dataSource
                        Log.i("TAG", "Active subscription for data type:  ")
                    }

                    if (subscriptions.isEmpty()) {
                        Log.i("TAG", "No active subscriptions")
                    }
                }
        // [END list_current_subscriptions]
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun enableBackgroundJob() {

        val intent = Intent(cordova.context, MyDataUpdateService::class.java)
        val pendingIntent =
                PendingIntent.getService(cordova.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        //SensorClien

        val dataSourceStep = DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_RAW)
                .build()

        Fitness.getRecordingClient(
                cordova.context,
                GoogleSignIn.getAccountForExtension(cordova.context, fitnessOptions)
        )
                // This example shows subscribing to a DataType, across all possible data
                // sources. Alternatively, a specific DataSource can be used.
                .subscribe(dataSourceStep)
                .addOnSuccessListener {
                    Log.i("Access GoogleFit:", "Successfully subscribed! SensorRequest")
                }
                .addOnFailureListener { e ->
                    Log.w("Access GoogleFit:", "There was a problem subscribing.", e)
                }

        Fitness.getSensorsClient(cordova.activity, account!!)
                .add(
                        SensorRequest.Builder()
                                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                .setSamplingRate(1, TimeUnit.MINUTES) // sample once per minute
                                .build()
                ) {
                    Toast.makeText(cordova.context, "UPDATE TYPE_STEP_COUNT_DELTA1", Toast.LENGTH_SHORT)
                            .show()
                    Log.i("OnDataPointListener:", it.toString())
                }
                .addOnSuccessListener {
                    Log.i("Access GoogleFit:", "SensorRequest")
                }

        Fitness.getSensorsClient(cordova.activity, account!!)
                .add(
                        SensorRequest.Builder()
                                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                .setSamplingRate(1, TimeUnit.MINUTES) // sample once per minute
                                .build(),
                        pendingIntent
                )
                .addOnSuccessListener {
                    Log.i("Access GoogleFit:", "SensorRequest")
                }


        //History

        val dataSource = DataSource.Builder()
                .setDataType(DataType.TYPE_WEIGHT)
                .setType(DataSource.TYPE_RAW)
                .build()


        Fitness.getRecordingClient(
                cordova.context,
                GoogleSignIn.getAccountForExtension(cordova.context, fitnessOptions)
        )
                // This example shows subscribing to a DataType, across all possible data
                // sources. Alternatively, a specific DataSource can be used.
                .subscribe(dataSource)
                .addOnSuccessListener {
                    Log.i("Access GoogleFit:", "Successfully subscribed!")
                }
                .addOnFailureListener { e ->
                    Log.w("Access GoogleFit:", "There was a problem subscribing.", e)
                }


        val request = DataUpdateListenerRegistrationRequest.Builder()
                .setDataType(DataType.TYPE_WEIGHT)
                .setPendingIntent(pendingIntent)
                .build()

        Fitness.getHistoryClient(
                cordova.context,
                GoogleSignIn.getAccountForExtension(cordova.context, fitnessOptions)
        )
                .registerDataUpdateListener(request)
                .addOnSuccessListener {
                    Log.i("Access GoogleFit:", "DataUpdateListener registered")
                }
    }

    //this should handle the response to the OAuth permissions
    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> {
                getData()
            }
        }
    }

    //this will handle the response to the permission dialog for ACTIVITY_RECOGNITION and ACCESS_COARSE_LOCATION
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            ACTIVITY_LOCATION_PERMISSIONS_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    checkAndGrantPermissions()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
                return
            }
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

    companion object {
        const val ACTIVITY_LOCATION_PERMISSIONS_REQUEST_CODE = 1
        const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 2
    }
}