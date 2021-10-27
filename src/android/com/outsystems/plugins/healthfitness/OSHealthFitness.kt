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
import com.outsystems.plugins.healthfitnesslib.HealthFitnessError
import com.outsystems.plugins.healthfitnesslib.store.AdvancedQueryParameters
import com.outsystems.plugins.healthfitnesslib.store.HealthFitnessManager
import com.outsystems.plugins.healthfitnesslib.store.HealthStore
import com.outsystems.plugins.oscordova.CordovaImplementation
import org.apache.cordova.*
import org.json.JSONArray

class OSHealthFitness : CordovaImplementation() {
    override var callbackContext: CallbackContext? = null

    var healthStore: HealthStore? = null
    val gson by lazy { Gson() }

    override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
        super.initialize(cordova, webView)
        val manager = HealthFitnessManager(cordova.context, cordova.activity)
        healthStore = HealthStore(this, manager)
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

        healthStore?.initAndRequestPermissions(
            customPermissions,
            allVariables,
            fitnessVariables,
            healthVariables,
            profileVariables,
            summaryVariables)
        checkAndGrantPermissions()
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
            healthStore?.requestGoogleFitPermissions()
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
        healthStore?.advancedQuery(parameters)
    }

    private fun writeData(args: JSONArray) {

        //process parameters
        val variable = args.getString(0)
        val value = args.getDouble(1).toFloat()

        healthStore?.updateData(variable, value)
    }

    private fun getLastRecord(args: JSONArray) {
        //process parameters
        val variable = args.getString(0)
        healthStore?.getLastRecord(variable)
    }

    private fun setBackgroundJob(args: JSONArray) {
        //process parameters
        val parameters = gson.fromJson(args.getString(0), BackgroundJobParameters::class.java)
        healthStore?.setBackgroundJob(parameters)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        //super.onActivityResult(requestCode, resultCode, intent)
        healthStore?.handleActivityResult(requestCode, resultCode, intent)
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
        grantResults: IntArray
    ) {
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