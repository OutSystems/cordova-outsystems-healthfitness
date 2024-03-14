package com.outsystems.plugins.healthfitness

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.gson.Gson
import com.outsystems.osnotificationpermissions.*
import com.outsystems.plugins.healthfitness.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitness.background.DatabaseManager
import com.outsystems.plugins.healthfitness.background.UpdateBackgroundJobParameters
import com.outsystems.plugins.healthfitness.data.types.HealthAdvancedQueryParameters
import com.outsystems.plugins.healthfitness.store.*
import com.outsystems.plugins.oscordova.CordovaImplementation
import org.apache.cordova.*
import org.json.JSONArray
import org.json.JSONException

class OSHealthFitness : CordovaImplementation() {
    override var callbackContext: CallbackContext? = null

    var healthStore: HealthStoreInterface? = null
    val gson by lazy { Gson() }
    private lateinit var healthConnectViewModel: HealthConnectViewModel
    private lateinit var healthConnectRepository: HealthConnectRepository
    private lateinit var healthConnectDataManager: HealthConnectDataManager
    private lateinit var healthConnectHelper: HealthConnectHelper
    private lateinit var alarmManagerHelper: AlarmManagerHelper
    private lateinit var backgroundParameters: BackgroundJobParameters

    private lateinit var alarmManager: AlarmManager

    // we need this variable because onResume is being called when
    // returning from the SCHEDULE_EXACT_ALARM permission screen
    private var requestingExactAlarmPermission = false

    // variables to hold foreground notification title and description
    // these values are defined in build time so we only need to read
    // them once on the initialize method
    private lateinit var foregroundNotificationTitle: String
    private lateinit var foregroundNotificationDescription: String

    override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
        super.initialize(cordova, webView)
        val manager = HealthFitnessManager(cordova.context, cordova.activity)
        val database = DatabaseManager(cordova.context)
        healthStore = HealthStore(cordova.context.applicationContext.packageName, manager, database)

        healthConnectDataManager = HealthConnectDataManager(database)
        healthConnectRepository = HealthConnectRepository(healthConnectDataManager)
        healthConnectHelper = HealthConnectHelper()
        alarmManagerHelper = AlarmManagerHelper()
        healthConnectViewModel =
            HealthConnectViewModel(healthConnectRepository, healthConnectHelper, alarmManagerHelper)
        alarmManager = getContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // get foreground notification title and description from resources (strings.xml)
        foregroundNotificationTitle = getContext().resources.getString(
            getActivity().resources.getIdentifier(
                "background_notification_title",
                "string",
                getActivity().packageName
            )
        )
        foregroundNotificationDescription = getContext().resources.getString(
            getActivity().resources.getIdentifier(
                "background_notification_description",
                "string",
                getActivity().packageName
            )
        )

    }

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {

        this.callbackContext = callbackContext

        if (!areGooglePlayServicesAvailable()) {
            return false
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
            "disconnectFromHealthConnect" -> {
                disconnectFromHealthConnect()
            }
            "openHealthConnect" -> {
                openHealthConnect()
            }
        }
        return true
    }

    // onResume is called when returning from the SCHEDULE_EXACT_ALARM permission screen
    override fun onResume(multitasking: Boolean) {
        if (requestingExactAlarmPermission) {
            requestingExactAlarmPermission = false
            onScheduleExactAlarmPermissionResult()
        }
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
                privacyPolicyUrl = getActivity().resources.getString(getActivity().resources.getIdentifier("privacy_policy_url", "string", getActivity().packageName)),
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
        val parameters = gson.fromJson(args.getString(0), HealthAdvancedQueryParameters::class.java)
        healthConnectViewModel.advancedQuery(
            parameters,
            getContext(),
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
        try {
            val variable = args.getString(0)
            val healthRecord = HealthRecord.valueOf(variable)
            val value = args.getDouble(1)

            healthConnectViewModel.writeData(
                healthRecord,
                value,
                getActivity().packageName,
                {
                    sendPluginResult("success", null)
                },
                {
                    sendPluginResult(null, Pair(it.code.toString(), it.message))
                }
            )
        } catch (e: Exception) {
            sendPluginResult(null, Pair(HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.code.toString(), HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.message))
        }
    }

    private fun getLastRecord(args: JSONArray) {
        try {
            healthConnectViewModel.getLastRecord(
                HealthRecord.valueOf(args.getString(0)),
                {
                    sendPluginResult(it, null)
                },
                {
                    sendPluginResult(null, Pair(it.code.toString(), it.message))
                }
            )
        } catch (e: Exception) {
            sendPluginResult(null, Pair(HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.code.toString(), HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.message))
        }

    }

    /**
     * Navigates to the permission screen for exact alarms or
     * skips it and request the other necessary permissions.
     * Also stores the background job parameters in a global variable to be used later.
     */
    private fun setBackgroundJob(args: JSONArray) {
        // save arguments for later use
        backgroundParameters = gson.fromJson(args.getString(0), BackgroundJobParameters::class.java)

        //request permission for exact alarms if necessary
        if (SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) {
            requestingExactAlarmPermission = true
            // we only need to request this permission if exact alarms need to be used
            // when there's another way to schedule background jobs to run, we can avoid this for some variables (e.g. steps)
            // we intended to use the Activity Recognition API, but it currently has a bug already reported to Google
            getContext().startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
        } else { // we can move on to other permissions if we don't need to request exact alarm permissions
            requestBackgroundJobPermissions()
        }
    }

    /**
     * Requests the POST_NOTIFICATIONS and ACTIVITY_RECOGNITION permissions.
     */
    private fun requestBackgroundJobPermissions() {
        val permissions = mutableListOf<String>().apply {
            if (SDK_INT >= 33) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (SDK_INT >= 29) {
                add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }.toTypedArray()

        PermissionHelper.requestPermissions(this, BACKGROUND_JOB_PERMISSIONS_REQUEST_CODE, permissions)
    }

    /**
     * Handles user response to exact alarm permission request.
     *
     */
    private fun onScheduleExactAlarmPermissionResult() {
        val permissionDenied = SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()
        if (permissionDenied) {
            // send plugin result with error
            sendPluginResult(
                null,
                Pair(
                    HealthFitnessError.BACKGROUND_JOB_EXACT_ALARM_PERMISSION_DENIED_ERROR.code.toString(),
                    HealthFitnessError.BACKGROUND_JOB_EXACT_ALARM_PERMISSION_DENIED_ERROR.message
                )
            )
            return
        }
        requestBackgroundJobPermissions()
    }

    /**
     * Sets a background job by calling the setBackgroundJob method of the ViewModel
     */
    private fun setBackgroundJobWithParameters(parameters: BackgroundJobParameters) {
        healthConnectViewModel.setBackgroundJob(
            parameters,
            foregroundNotificationTitle,
            foregroundNotificationDescription,
            getContext(),
            {
                sendPluginResult("success", null)
            },
            {
                sendPluginResult(null, Pair(it.code.toString(), it.message))
            }
        )
    }

    private fun deleteBackgroundJob(args: JSONArray) {
        val jobId = args.getString(0)
        healthConnectViewModel.deleteBackgroundJob(
            jobId,
            getContext(),
            {
                sendPluginResult("success", null)
            },
            {
                sendPluginResult(null, Pair(it.code.toString(), it.message))
            }
        )
    }

    private fun listBackgroundJobs() {
        healthConnectViewModel.listBackgroundJobs(
            {
                val pluginResponseJson = gson.toJson(it)
                sendPluginResult(pluginResponseJson)
            },
            {
                sendPluginResult(null, Pair(it.code.toString(), it.message))
            }
        )
    }

    private fun updateBackgroundJob(args: JSONArray) {
        val parameters = gson.fromJson(args.getString(0), UpdateBackgroundJobParameters::class.java)
        healthConnectViewModel.updateBackgroundJob(
            parameters,
            {
                sendPluginResult("success", null)
            },
            {
                sendPluginResult(null, Pair(it.code.toString(), it.message))
            }
        )
    }

    @Deprecated(
        message = "The Google Fit Android API is deprecated. " +
                "To fully disconnect from the legacy Google Fit integration, " +
                "please visit your Google Account settings and " +
                "revoke the OAuth token associated with the app.",
        replaceWith = ReplaceWith("disconnectFromHealthConnect()")
    )
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

    private fun disconnectFromHealthConnect() {
        healthConnectViewModel.disconnectFromHealthConnect(
            getActivity(),
            {
                sendPluginResult("success", null)
            },
            {
                sendPluginResult(null, Pair(it.code.toString(), it.message))
            }
        )
    }

    private fun openHealthConnect() {
        healthConnectViewModel.openHealthConnect(
            getContext(),
            {
                sendPluginResult("success", null)
            },
            {
                sendPluginResult(null, Pair(it.code.toString(), it.message))
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
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
            BACKGROUND_JOB_PERMISSIONS_REQUEST_CODE -> {
                for (result in grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        sendPluginResult(
                            null,
                            Pair(
                                HealthFitnessError.BACKGROUND_JOB_PERMISSIONS_DENIED_ERROR.code.toString(),
                                HealthFitnessError.BACKGROUND_JOB_PERMISSIONS_DENIED_ERROR.message
                            )
                        )
                        return
                    }
                }
                setBackgroundJobWithParameters(backgroundParameters)
            }
        }
    }

    companion object {
        const val ACTIVITY_LOCATION_PERMISSIONS_REQUEST_CODE = 1
        const val BACKGROUND_JOB_PERMISSIONS_REQUEST_CODE = 2
    }
}