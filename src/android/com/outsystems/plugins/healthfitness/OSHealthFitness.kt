package com.outsystems.plugins.healthfitness

import TimeUnitSerializer
import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build.VERSION.SDK_INT
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.outsystems.plugins.healthfitness.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitness.background.DatabaseManager
import com.outsystems.plugins.healthfitness.background.UpdateBackgroundJobParameters
import com.outsystems.plugins.healthfitness.data.Constants
import com.outsystems.plugins.healthfitness.data.HealthEnumTimeUnit
import com.outsystems.plugins.healthfitness.data.HealthFitnessError
import com.outsystems.plugins.healthfitness.data.HealthRecord
import com.outsystems.plugins.healthfitness.data.types.HealthAdvancedQueryParameters
import com.outsystems.plugins.healthfitness.data.types.HealthFitnessGroupPermission
import com.outsystems.plugins.healthfitness.data.types.HealthFitnessPermission
import com.outsystems.plugins.healthfitness.helpers.ActivityTransitionHelper
import com.outsystems.plugins.healthfitness.helpers.AlarmManagerHelper
import com.outsystems.plugins.healthfitness.helpers.HealthConnectHelper
import com.outsystems.plugins.healthfitness.repository.HealthConnectRepository
import com.outsystems.plugins.healthfitness.store.*
import com.outsystems.plugins.healthfitness.viewmodel.HealthConnectDataManager
import com.outsystems.plugins.healthfitness.viewmodel.HealthConnectViewModel
import org.apache.cordova.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class OSHealthFitness : CordovaPlugin() {
    var callbackContext: CallbackContext? = null

    val gson by lazy { Gson() }
    private lateinit var healthConnectViewModel: HealthConnectViewModel
    private lateinit var healthConnectRepository: HealthConnectRepository
    private lateinit var healthConnectDataManager: HealthConnectDataManager
    private lateinit var healthConnectHelper: HealthConnectHelper
    private lateinit var alarmManagerHelper: AlarmManagerHelper
    private lateinit var activityTransitionHelper: ActivityTransitionHelper
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
        val database = DatabaseManager(cordova.context)

        healthConnectDataManager = HealthConnectDataManager(database)
        healthConnectRepository = HealthConnectRepository(healthConnectDataManager)
        healthConnectHelper = HealthConnectHelper()
        alarmManagerHelper = AlarmManagerHelper()
        activityTransitionHelper = ActivityTransitionHelper()
        healthConnectViewModel =
            HealthConnectViewModel(
                healthConnectRepository,
                healthConnectHelper,
                alarmManagerHelper,
                activityTransitionHelper
            )
        alarmManager = cordova.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // get foreground notification title and description from resources (strings.xml)
        foregroundNotificationTitle = cordova.context.resources.getString(
            cordova.activity.resources.getIdentifier(
                "background_notification_title",
                "string",
                cordova.context.packageName
            )
        )
        foregroundNotificationDescription = cordova.context.resources.getString(
            cordova.activity.resources.getIdentifier(
                "background_notification_description",
                "string",
                cordova.activity.packageName
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
                initAndRequestPermissions(args, callbackContext)
            }

            "getData" -> {
                advancedQuery(args, callbackContext)
            }

            "writeData" -> {
                writeData(args, callbackContext)
            }

            "getLastRecord" -> {
                getLastRecord(args, callbackContext)
            }

            "setBackgroundJob" -> {
                setBackgroundJob(args)
            }

            "deleteBackgroundJob" -> {
                deleteBackgroundJob(args, callbackContext)
            }

            "listBackgroundJobs" -> {
                listBackgroundJobs(callbackContext)
            }

            "updateBackgroundJob" -> {
                updateBackgroundJob(args, callbackContext)
            }

            "disconnectFromHealthConnect" -> {
                disconnectFromHealthConnect(callbackContext)
            }

            "openHealthConnect" -> {
                openHealthConnect(callbackContext)
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

    private fun initAndRequestPermissions(args: JSONArray, callbackContext: CallbackContext) {
        try {
            healthConnectViewModel.initAndRequestPermissions(
                cordova.activity,
                gson.fromJson(args.getString(0), Array<HealthFitnessPermission>::class.java),
                gson.fromJson(args.getString(1), HealthFitnessGroupPermission::class.java),
                gson.fromJson(args.getString(2), HealthFitnessGroupPermission::class.java),
                gson.fromJson(args.getString(3), HealthFitnessGroupPermission::class.java),
                gson.fromJson(args.getString(4), HealthFitnessGroupPermission::class.java),
                privacyPolicyUrl = cordova.activity.resources.getString(
                    cordova.activity.resources.getIdentifier(
                        "privacy_policy_url",
                        "string",
                        cordova.activity.packageName
                    )
                ),
                { cordova.setActivityResultCallback(this) },
                { sendError(callbackContext, it) }
            )
        } catch (hse: HealthStoreException) {
            sendError(callbackContext, hse.error)
        } catch (e: JSONException) {
            sendError(callbackContext, HealthFitnessError.PARSING_PARAMETERS_ERROR)
        } catch (e: Exception) {
            sendError(callbackContext, HealthFitnessError.REQUEST_PERMISSIONS_GENERAL_ERROR)
        }
    }

    private fun advancedQuery(args: JSONArray, callbackContext: CallbackContext) {
        var deprecationWarning = false
        val onDeprecatedUsage: () -> Unit = {
            deprecationWarning = true
        }
        val customGson = GsonBuilder().registerTypeAdapter(
            HealthEnumTimeUnit::class.java,
            TimeUnitSerializer(onDeprecatedUsage)
        )
            .create()
        val parameters =
            customGson.fromJson(args.getString(0), HealthAdvancedQueryParameters::class.java)
        healthConnectViewModel.advancedQuery(
            parameters,
            cordova.context,
            { response ->
                if (!deprecationWarning)
                    sendSuccess(callbackContext, response)
                else {
                    sendSuccessWithWarning(
                        callbackContext,
                        response,
                        OSHealthFitnessWarning.DEPRECATED_TIME_UNIT
                    )
                }
            },
            { error -> sendError(callbackContext, error) }
        )
    }

    private fun writeData(args: JSONArray, callbackContext: CallbackContext) {
        try {
            val variable = args.getString(0)
            val healthRecord = HealthRecord.valueOf(variable)
            val value = args.getDouble(1)

            healthConnectViewModel.writeData(
                healthRecord,
                value,
                cordova.activity.packageName,
                {
                    sendSuccess(callbackContext)
                },
                {
                    sendError(callbackContext, it)
                }
            )
        } catch (e: Exception) {
            sendError(callbackContext, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR)
        }
    }

    private fun getLastRecord(args: JSONArray, callbackContext: CallbackContext) {
        try {
            healthConnectViewModel.getLastRecord(
                HealthRecord.valueOf(args.getString(0)),
                { sendSuccess(callbackContext, it) },
                { sendError(callbackContext, it) }
            )
        } catch (e: Exception) {
            sendError(callbackContext, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR)
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
        if (!Constants.ACTIVITY_VARIABLES.contains(backgroundParameters.variable) && SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) {
            requestingExactAlarmPermission = true
            // we only need to request this permission if exact alarms need to be used
            // when the variable is an activity variable (e.g. steps),
            // we use the Activity Recognition Transition API instead of exact alarms.
            cordova.context.startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
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

        PermissionHelper.requestPermissions(
            this,
            BACKGROUND_JOB_PERMISSIONS_REQUEST_CODE,
            permissions
        )
    }

    /**
     * Handles user response to exact alarm permission request.
     *
     */
    private fun onScheduleExactAlarmPermissionResult() {
        val permissionDenied = SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()
        if (permissionDenied) {
            // send plugin result with error
            callbackContext?.let {
                sendError(
                    it,
                    HealthFitnessError.BACKGROUND_JOB_EXACT_ALARM_PERMISSION_DENIED_ERROR
                )
            }
            return
        }
        requestBackgroundJobPermissions()
    }

    /**
     * Requests permission to read health data in the background for API 35,
     * or calls setBackgroundJobWithParameters otherwise
     */
    private fun requestReadDataBackgroundPermission(callbackContext: CallbackContext) {
        if (SDK_INT >= 35) {
            cordova.setActivityResultCallback(this)
            healthConnectViewModel.requestReadDataBackgroundPermission(this.cordova.activity)
        } else {
            setBackgroundJobWithParameters(backgroundParameters, callbackContext)
        }
    }

    /**
     * Sets a background job by calling the setBackgroundJob method of the ViewModel
     */
    private fun setBackgroundJobWithParameters(
        parameters: BackgroundJobParameters,
        callbackContext: CallbackContext
    ) {
        healthConnectViewModel.setBackgroundJob(
            parameters,
            foregroundNotificationTitle,
            foregroundNotificationDescription,
            cordova.context,
            { sendSuccess(callbackContext) },
            { sendError(callbackContext, it) }
        )
    }

    private fun deleteBackgroundJob(args: JSONArray, callbackContext: CallbackContext) {
        val jobId = args.getString(0)
        healthConnectViewModel.deleteBackgroundJob(
            jobId,
            cordova.context,
            { sendSuccess(callbackContext) },
            { sendError(callbackContext, it) }
        )
    }

    private fun listBackgroundJobs(callbackContext: CallbackContext) {
        healthConnectViewModel.listBackgroundJobs(
            { sendSuccess(callbackContext, it) },
            { sendError(callbackContext, it) }
        )
    }

    private fun updateBackgroundJob(args: JSONArray, callbackContext: CallbackContext) {
        val parameters = gson.fromJson(args.getString(0), UpdateBackgroundJobParameters::class.java)
        healthConnectViewModel.updateBackgroundJob(
            parameters,
            { sendSuccess(callbackContext) },
            { sendError(callbackContext, it) }
        )
    }

    private fun disconnectFromHealthConnect(callbackContext: CallbackContext) {
        healthConnectViewModel.disconnectFromHealthConnect(
            cordova.activity,
            { sendSuccess(callbackContext) },
            { sendError(callbackContext, it) }
        )
    }

    private fun openHealthConnect(callbackContext: CallbackContext) {
        healthConnectViewModel.openHealthConnect(
            cordova.context,
            { sendSuccess(callbackContext) },
            { sendError(callbackContext, it) }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        //check if result comes for requesting READ_HEALTH_DATA_IN_BACKGROUND permission
        intent?.let {
            if (intent.getBooleanExtra(Constants.EXTRA_CONTAINS_READ_DATA_BACKGROUND, false)) {
                if (intent.getIntExtra(
                        Constants.EXTRA_RESULT_PERMISSION_KEY_GLOBAL,
                        Constants.EXTRA_RESULT_PERMISSION_DENIED
                    ) == Constants.EXTRA_RESULT_PERMISSION_GRANTED
                ) {
                    callbackContext?.let {
                        setBackgroundJobWithParameters(
                            backgroundParameters,
                            it
                        )
                    }
                    return
                }
                callbackContext?.let {
                    sendError(
                        it,
                        HealthFitnessError.BACKGROUND_JOB_READ_DATA_PERMISSION_DENIED
                    )
                }

                return
            }
        }

        // if result comes from requesting standard permissions
        healthConnectViewModel.handleActivityResult(requestCode, resultCode, intent,
            {
                callbackContext?.let { sendSuccess(it) }
            },
            { error -> callbackContext?.let { sendError(it, error) } }
        )
    }

    fun areGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(cordova.activity)

        if (status != ConnectionResult.SUCCESS) {
            var result: Pair<String, String>? = if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(cordova.activity, status, 1)?.show()
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
            callbackContext?.let {
                sendSuccess(it, result)
            }
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
            BACKGROUND_JOB_PERMISSIONS_REQUEST_CODE -> {
                for (result in grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        callbackContext?.let {
                            sendError(
                                it,
                                HealthFitnessError.BACKGROUND_JOB_PERMISSIONS_DENIED_ERROR
                            )
                        }
                        return
                    }
                }
                callbackContext?.let { requestReadDataBackgroundPermission(it) }
            }
        }
    }

    companion object {
        const val BACKGROUND_JOB_PERMISSIONS_REQUEST_CODE = 2
    }

    /**
     * Helper method to send a success result
     * @param callbackContext CallbackContext to send the result to
     */
    private fun sendSuccess(callbackContext: CallbackContext, result: Any? = null) {
        var pluginResult = PluginResult(PluginResult.Status.OK)
        result?.let {
            val resultJson = gson.toJson(result)
            pluginResult = PluginResult(PluginResult.Status.OK, resultJson)
        }
        callbackContext.sendPluginResult(pluginResult)
    }

    /**
     * Helper method to send a result with warning
     * @param callbackContext CallbackContext to send the result to
     * @param warning Warning to be sent in the result
     */
    private fun sendSuccessWithWarning(
        callbackContext: CallbackContext,
        result: Any?,
        warning: OSHealthFitnessWarning
    ) {
        val warningObject = JSONObject().apply {
            put("code", warning.code)
            put("message", warning.message)
        }
        val pluginResult = PluginResult(
            PluginResult.Status.OK,
            JSONObject().apply {
                put("response", gson.toJson(result))
                put("warning", warningObject)
            }
        )
        callbackContext.sendPluginResult(pluginResult)
    }


    /**
     * Helper method to send an error result
     * @param callbackContext CallbackContext to send the result to
     * @param error Error to be sent in the result
     */
    private fun sendError(callbackContext: CallbackContext, error: HealthFitnessError) {
        val pluginResult = PluginResult(
            PluginResult.Status.ERROR,
            JSONObject().apply {
                put("code", error.code)
                put("message", error.message)
            }
        )
        callbackContext.sendPluginResult(pluginResult)
    }

}
