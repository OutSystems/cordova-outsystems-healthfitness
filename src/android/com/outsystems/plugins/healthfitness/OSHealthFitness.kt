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
import com.outsystems.osnotificationpermissions.*
import com.outsystems.plugins.healthfitness.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitness.background.DatabaseManager
import com.outsystems.plugins.healthfitness.background.UpdateBackgroundJobParameters
import com.outsystems.plugins.healthfitness.store.*
import com.outsystems.plugins.oscordova.CordovaImplementation
import org.apache.cordova.*
import org.json.JSONArray
import org.json.JSONException

class OSHealthFitness : CordovaImplementation() {
    override var callbackContext: CallbackContext? = null

    var healthStore: HealthStoreInterface? = null
    val gson by lazy { Gson() }
    var notificationPermissions = OSNotificationPermissions()
    lateinit var healthConnectViewModel: HealthConnectViewModel
    lateinit var healthConnectRepository: HealthConnectRepository
    lateinit var healthConnectDataManager: HealthConnectDataManager
    lateinit var healthConnectHelper: HealthConnectHelper

    override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
        super.initialize(cordova, webView)
        val manager = HealthFitnessManager(cordova.context, cordova.activity)
        val database = DatabaseManager(cordova.context)
        healthStore = HealthStore(cordova.context.applicationContext.packageName, manager, database)

        healthConnectDataManager = HealthConnectDataManager()
        healthConnectRepository = HealthConnectRepository(healthConnectDataManager)
        healthConnectHelper = HealthConnectHelper()
        healthConnectViewModel = HealthConnectViewModel(healthConnectRepository, healthConnectHelper)
    }

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {

        this.callbackContext = callbackContext

        if (!areGooglePlayServicesAvailable()) {
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
            "disconnectFromGoogleFit" -> {
                disconnectFromGoogleFit()
            }
        }
        return true
    }

    private fun initAndRequestPermissions(args: JSONArray) {
        try {
            healthConnectViewModel.initAndRequestPermissions(
                getActivity(),
                gson.fromJson(args.getString(0), Array<HealthFitnessPermission>::class.java),
                gson.fromJson(args.getString(1), HealthFitnessGroupPermission::class.java),
                gson.fromJson(args.getString(2), HealthFitnessGroupPermission::class.java),
                gson.fromJson(args.getString(3), HealthFitnessGroupPermission::class.java),
                gson.fromJson(args.getString(4), HealthFitnessGroupPermission::class.java),
                {
                    setAsActivityResultCallback()
                },
                {
                    sendPluginResult(null, Pair(it.code.toString(), it.message))
                }
            )
        } catch (hse: HealthStoreException) {
            sendPluginResult(null, Pair(hse.error.code.toString(), hse.error.message))
        } catch (e: JSONException) {
            sendPluginResult(
                null,
                Pair(
                    HealthFitnessError.PARSING_PARAMETERS_ERROR.code.toString(),
                    HealthFitnessError.PARSING_PARAMETERS_ERROR.message
                )
            )
        } catch (e: Exception) {
            sendPluginResult(
                null,
                Pair(
                    HealthFitnessError.REQUEST_PERMISSIONS_GENERAL_ERROR.code.toString(),
                    HealthFitnessError.REQUEST_PERMISSIONS_GENERAL_ERROR.message
                )
            )
        }
    }


    private fun areAndroidPermissionsGranted(permissions: List<String>): Boolean {
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(
                    getActivity(),
                    it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun checkAndGrantPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BODY_SENSORS
        )

        if (SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if (areAndroidPermissionsGranted(permissions)) {
            if (!healthStore!!.areGoogleFitPermissionsGranted()) {
                setAsActivityResultCallback()
            }
            if (healthStore?.requestGoogleFitPermissions() == true) {
                sendPluginResult("success")
            }
        } else {
            PermissionHelper.requestPermissions(
                this,
                ACTIVITY_LOCATION_PERMISSIONS_REQUEST_CODE,
                permissions.toTypedArray()
            )
        }
    }

    private fun advancedQuery(args: JSONArray) {
        val parameters = gson.fromJson(args.getString(0), AdvancedQueryParameters::class.java)
        healthStore?.advancedQueryAsync(
            parameters,
            { response ->
                val pluginResponseJson = gson.toJson(response)
                sendPluginResult(pluginResponseJson)
            },
            { error ->
                sendPluginResult(null, Pair(error.code.toString(), error.message))
            }
        )
    }

    private fun writeData(args: JSONArray) {
        val variable = args.getString(0)
        val value = args.getDouble(1)
        val healthRecordName: HealthRecordName

        when (variable) {
            HealthRecordName.WEIGHT.name -> {
                healthRecordName = HealthRecordName.WEIGHT
            }
            HealthRecordName.HEIGHT.name -> {
                healthRecordName = HealthRecordName.HEIGHT
            }
            HealthRecordName.BODY_FAT_PERCENTAGE.name -> {
                healthRecordName = HealthRecordName.BODY_FAT_PERCENTAGE
            }
            HealthRecordName.BASAL_METABOLIC_RATE.name -> {
                healthRecordName = HealthRecordName.BASAL_METABOLIC_RATE
            }
            else -> {
                sendPluginResult(
                    null,
                    Pair(
                        HealthFitnessError.WRITE_DATA_NOT_PROFILE_ERROR.code.toString(),
                        HealthFitnessError.WRITE_DATA_NOT_PROFILE_ERROR.message
                    )
                )
                return
            }
        }

        healthConnectViewModel.writeData(
            healthRecordName,
            value,
            getActivity().packageName,
            {
                sendPluginResult("success", null)
            },
            {
                sendPluginResult(null, Pair(it.code.toString(), it.message))
            }
        )
    }

    private fun getLastRecord(args: JSONArray) {
        //process parameters
        val variable = args.getString(0)
        healthConnectViewModel.getLastRecord()
    }

    private fun setBackgroundJob(args: JSONArray) {
        notificationPermissions.requestNotificationPermission(
            this,
            ACTIVITY_NOTIFICATION_PERMISSIONS_REQUEST_CODE
        )

        //process parameters
        val parameters = gson.fromJson(args.getString(0), BackgroundJobParameters::class.java)
        healthStore?.setBackgroundJob(
            parameters,
            { response ->
                sendPluginResult(response)
            },
            { error ->
                sendPluginResult(null, Pair(error.code.toString(), error.message))
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
                sendPluginResult(null, Pair(error.code.toString(), error.message))
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
                sendPluginResult(null, Pair(error.code.toString(), error.message))
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
                sendPluginResult(null, Pair(error.code.toString(), error.message))
            }
        )
    }

    private fun disconnectFromGoogleFit() {
        healthStore?.disconnectFromGoogleFit(
            {
                sendPluginResult("success", null)
            },
            {
                sendPluginResult(null, Pair(it.code.toString(), it.message))
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        super.onActivityResult(requestCode, resultCode, intent)
        healthConnectViewModel.handleActivityResult(requestCode, resultCode, intent,
            {
                sendPluginResult("success", null)
            },
            {
                sendPluginResult(null, Pair(it.code.toString(), it.message))
            }
        )
    }

    override fun areGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(getActivity())

        if (status != ConnectionResult.SUCCESS) {
            var result: Pair<String, String>? = null
            result = if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(getActivity(), status, 1)?.show()
                Pair(
                    HealthFitnessError.GOOGLE_SERVICES_RESOLVABLE_ERROR.code.toString(),
                    HealthFitnessError.GOOGLE_SERVICES_RESOLVABLE_ERROR.message
                )
            } else {
                Pair(
                    HealthFitnessError.GOOGLE_SERVICES_ERROR.code.toString(),
                    HealthFitnessError.GOOGLE_SERVICES_ERROR.message
                )
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
        const val ACTIVITY_NOTIFICATION_PERMISSIONS_REQUEST_CODE = 2
    }
}