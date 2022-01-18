package com.outsystems.plugins.healthfitness

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.gson.Gson
import com.outsystems.plugins.libs.healthandfitness.HealthFitnessError
import com.outsystems.plugins.libs.healthandfitness.background.BackgroundJobParameters
import com.outsystems.plugins.libs.healthandfitness.background.UpdateBackgroundJobParameters
import com.outsystems.plugins.libs.healthandfitness.background.database.DatabaseManager
import com.outsystems.plugins.libs.healthandfitness.store.*
import com.outsystems.plugins.oscordova.CordovaImplementation

import org.apache.cordova.*
import org.json.JSONArray

class OSHealthFitness : CordovaImplementation() {
    override var callbackContext: CallbackContext? = null

    var healthStore: HealthStoreInterface? = null
    val gson by lazy { Gson() }

    override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
        super.initialize(cordova, webView)
        val manager = HealthFitnessManager(cordova.context, cordova.activity)
        val database = DatabaseManager(cordova.context)
        healthStore = HealthStore(cordova.context.applicationContext.packageName, manager, database)
    }

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        this.callbackContext = callbackContext

        if(!areGooglePlayServicesAvailable()) {
            return false;
        }

        when (action) {
            "requestPermissions" -> {
                initAndRequestPermissions(args)
            }
            "getData" -> {
                advancedQuery(args)
            }
            "writeData" -> {
                writeData(args)
            }
            "getLastRecord" -> {
                getLastRecord(args)
            }
            "setBackgroundJob" -> {
                setBackgroundJob(args)
            }
            "deleteBackgroundJob" -> {
                deleteBackgroundJob(args)
            }
            "listBackgroundJobs" -> {
                listBackgroundJobs()
            }
            "updateBackgroundJob" -> {
                updateBackgroundJob(args)
            }
        }
        return true
    }

    //create array of permission oauth
    private fun initAndRequestPermissions(args : JSONArray) {
        val customPermissions = args.getString(0)
        val allVariables = args.getString(1)
        val fitnessVariables = args.getString(2)
        val healthVariables = args.getString(3)
        val profileVariables = args.getString(4)
        val summaryVariables = args.getString(5)

        val customVariablesPermissions = gson.fromJson(customPermissions, Array<GoogleFitPermission>::class.java)
        val allVariablesPermissions = gson.fromJson(allVariables, GoogleFitGroupPermission::class.java)
        val fitnessVariablesPermissions = gson.fromJson(fitnessVariables, GoogleFitGroupPermission::class.java)
        val healthVariablesPermissions = gson.fromJson(healthVariables, GoogleFitGroupPermission::class.java)
        val profileVariablesPermissions = gson.fromJson(profileVariables, GoogleFitGroupPermission::class.java)
        val summaryVariablesPermissions = gson.fromJson(summaryVariables, GoogleFitGroupPermission::class.java)

        try {
            healthStore?.initAndRequestPermissions(
                customVariablesPermissions,
                allVariablesPermissions,
                fitnessVariablesPermissions,
                healthVariablesPermissions,
                profileVariablesPermissions,
                summaryVariablesPermissions)
            checkAndGrantPermissions()
        }
        catch (hse : HealthStoreException) {
            sendPluginResult(null, Pair(hse.error.code, hse.error.message))
        }
    }

    private fun areAndroidPermissionsGranted(permissions: List<String>): Boolean {
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(cordova.activity, it) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun checkAndGrantPermissions(){
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BODY_SENSORS
        )

        if(SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if (areAndroidPermissionsGranted(permissions)) {
            if(!healthStore!!.areGoogleFitPermissionsGranted()){
                setAsActivityResultCallback()
            }
            if(healthStore?.requestGoogleFitPermissions() == true) {
                sendPluginResult("success")
            }
        }
        else {
            PermissionHelper.requestPermissions(
                this,
                ACTIVITY_LOCATION_PERMISSIONS_REQUEST_CODE,
                permissions.toTypedArray()
            )
        }
    }

    private fun advancedQuery(args : JSONArray) {
        val parameters = gson.fromJson(args.getString(0), AdvancedQueryParameters::class.java)
        healthStore?.advancedQueryAsync(
            parameters,
            { response ->
                val pluginResponseJson = gson.toJson(response)
                sendPluginResult(pluginResponseJson)
            },
            { error ->
                sendPluginResult(null, Pair(error.code, error.message))
            }
        )
    }

    private fun writeData(args: JSONArray) {

        //process parameters
        val variable = args.getString(0)
        val value = args.getDouble(1).toFloat()

        healthStore?.updateDataAsync(
            variable,
            value,
            { response ->
                sendPluginResult(response)
            },
            { error ->
                sendPluginResult(null, Pair(error.code, error.message))
            }

        )
    }

    private fun getLastRecord(args: JSONArray) {
        //process parameters
        val variable = args.getString(0)
        healthStore?.getLastRecordAsync(
            variable,
            { response ->
                val pluginResponseJson = gson.toJson(response)
                sendPluginResult(pluginResponseJson)
            },
            { error ->
                sendPluginResult(null, Pair(error.code, error.message))
            })
    }

    private fun setBackgroundJob(args: JSONArray) {
        //process parameters
        val parameters = gson.fromJson(args.getString(0), BackgroundJobParameters::class.java)
        healthStore?.setBackgroundJob(
            parameters,
            { response ->
                sendPluginResult(response)
            },
            { error ->
                sendPluginResult(null, Pair(error.code, error.message))
            }
        )
    }

    private fun deleteBackgroundJob(args: JSONArray) {
        val parameters = args.getString(0)
        healthStore?.deleteBackgroundJob(
            parameters,
            { response ->
                sendPluginResult(response)
            },
            { error ->
                sendPluginResult(null, Pair(error.code, error.message))
            }
        )
    }
                
    private fun listBackgroundJobs() {
        healthStore?.listBackgroundJobs(
            { response ->
                val pluginResponseJson = gson.toJson(response)
                sendPluginResult(pluginResponseJson)
            },
            { error ->
                sendPluginResult(null, Pair(error.code, error.message))
            }
        )
    }

    private fun updateBackgroundJob(args: JSONArray) {
        val parameters = gson.fromJson(args.getString(0), UpdateBackgroundJobParameters::class.java)
        healthStore?.updateBackgroundJob(
            parameters,
            { response ->
                sendPluginResult(response)
            },
            { error ->
                sendPluginResult(null, Pair(error.code, error.message))
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        //super.onActivityResult(requestCode, resultCode, intent)
        try {
            healthStore?.handleActivityResult(requestCode, resultCode, intent)
        }
        catch(hse : HealthStoreException) {
            val error = hse.error
            sendPluginResult(null, Pair(error.code, error.message))
        }
    }

    override fun areGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(cordova.activity)

        if (status != ConnectionResult.SUCCESS) {
            var result: Pair<Int, String>? = null
            result = if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(cordova.activity, status, 1).show()
                Pair(HealthFitnessError.GOOGLE_SERVICES_RESOLVABLE_ERROR.code, HealthFitnessError.GOOGLE_SERVICES_RESOLVABLE_ERROR.message)
            } else {
                Pair(HealthFitnessError.GOOGLE_SERVICES_ERROR.code, HealthFitnessError.GOOGLE_SERVICES_ERROR.message)
            }
            sendPluginResult(null, result)
            return false
        }
        return true
    }

    override fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        when (requestCode) {
            ACTIVITY_LOCATION_PERMISSIONS_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    checkAndGrantPermissions()
                } else {
                }
                return
            }
        }
    }

    companion object {
        const val ACTIVITY_LOCATION_PERMISSIONS_REQUEST_CODE = 1
    }
}