package com.outsystems.plugins.healthfitness.store

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
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
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.DataUpdateListenerRegistrationRequest
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.gson.Gson
import com.outsystems.plugins.healthfitness.AndroidPlatformInterface
import com.outsystems.plugins.healthfitness.OSHealthFitness
import com.outsystems.plugins.healthfitness.MyDataUpdateService
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

enum class EnumAccessType(val value : String) {
    READ("READ"),
    WRITE("WRITE"),
    READWRITE("READWRITE")
}

class  HealthStore(val platformInterface: AndroidPlatformInterface) {
    var context: Context = platformInterface.getContext()
    var activity: Activity = platformInterface.getActivity()
    
    private var fitnessOptions: FitnessOptions? = null
    private var account: GoogleSignInAccount? = null

    private val googleFitVariables: Map<String, GoogleFitVariable> by lazy {
        mapOf(
            "HEARTRATE" to GoogleFitVariable(DataType.TYPE_HEART_RATE_BPM),
            "STEPS" to GoogleFitVariable(DataType.TYPE_STEP_COUNT_DELTA),
            "WEIGHT" to GoogleFitVariable(DataType.TYPE_WEIGHT)
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initAndRequestPermissions(args: JSONArray) {

        val customPermissions = args.getString(0)

        /*
        val allVariables = args.getBoolean(1)
        val fitnessVariables = args.getBoolean(2)
        val healthVariables = args.getBoolean(3)
        val profileVariables = args.getBoolean(4)
        */

        val permissionList = parseCustomPermissions(customPermissions)

        initFitnessOptions(permissionList)
    }

    private fun parseCustomPermissions(permissionsJson : String) : List<Pair<DataType, Int>> {
        val result: MutableList<Pair<DataType, Int>> = mutableListOf()
        val permissions = Gson().fromJson(permissionsJson, Array<GoogleFitPermission>::class.java)
        permissions.forEach { permission ->
            googleFitVariables[permission.variable]?.let { googleVariable ->
                if(permission.accessType == EnumAccessType.WRITE.value) {
                    result.add(Pair(googleVariable.dataType, FitnessOptions.ACCESS_WRITE))
                }
                else if(permission.accessType == EnumAccessType.READWRITE.value){
                    result.add(Pair(googleVariable.dataType, FitnessOptions.ACCESS_READ))
                    result.add(Pair(googleVariable.dataType, FitnessOptions.ACCESS_WRITE))
                }
                else {
                    result.add(Pair(googleVariable.dataType, FitnessOptions.ACCESS_READ))
                }
            }
        }
        return result
    }

    private fun initFitnessOptions(permissionList: List<Pair<DataType, Int>>) {
        val fitnessBuild = FitnessOptions.builder()
        permissionList.forEach {
            fitnessBuild.addDataType(it.first, it.second)
        }
        fitnessOptions = fitnessBuild.build()
        account = GoogleSignIn.getAccountForExtension(context, fitnessOptions!!)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun requestGoogleFitPermissions() {
        fitnessOptions?.let {
            GoogleSignIn.requestPermissions(
                platformInterface.getActivity(),  // your activity
                OSHealthFitness.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,  // e.g. 1
                account,
                it
            )
        }
    }

    fun checkAllGoogleFitPermissionGranted(): Boolean {
        account.let {
            fitnessOptions.let {
                return GoogleSignIn.hasPermissions(account!!, fitnessOptions!!)
            }
        }
    }

    fun checkAllPermissionGranted(permissions: Array<String>): Boolean {
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(
                    platformInterface.getActivity(),
                    it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getDataAlt() {

        val end = LocalDateTime.now()
        val start = end.minusDays(1L)
        val endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond()
        val startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond()
        val context = context
        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_CALORIES_EXPENDED)
            .setTimeRange(startSeconds, endSeconds, TimeUnit.SECONDS)
            .setLimit(1)
            .build()
        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
        var resultVariable: Float? = null
        Fitness.getHistoryClient(context, account).readData(readRequest)
            .addOnSuccessListener { dataReadResponse: DataReadResponse ->
                resultVariable = dataReadResponse.dataSets[0].dataPoints.firstOrNull()
                    ?.getValue(Field.FIELD_CALORIES)?.asFloat()
                Log.d(
                    "Access GoogleFit:",
                    dataReadResponse.dataSets[0].dataPoints.firstOrNull()
                        ?.getValue(Field.FIELD_CALORIES).toString()
                )
            }
            .addOnFailureListener { dataReadResponse: Exception ->
                Log.d(
                    "TAG",
                    dataReadResponse.message!!
                )
            }
        platformInterface.sendPluginResult(resultVariable)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getData() {
        val endTime = LocalDateTime.of(2021,9,10,0,0,0).atZone(ZoneId.systemDefault())
        val startTime = endTime.minusDays(7)

        val datasource = DataSource.Builder()
            .setAppPackageName("com.google.android.gms")
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("estimated_steps")
            .build()
        val readRequest = DataReadRequest.Builder()
//            .read(DataType.TYPE_STEP_COUNT_DELTA)
            .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
//            .setLimit(100)
            .bucketByTime(1, TimeUnit.DAYS)
            .aggregate(datasource)
//            .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
            .build()
        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
        var resultVariable: String? = null
        Log.d(
            "Start date",
            startTime.dayOfMonth.toString() + "-" + startTime.month.toString() +  "-" + startTime.year.toString()
        )
        Log.d(
            "END date",
            endTime.dayOfMonth.toString() + "-" + endTime.month.toString() +  "-" + endTime.year.toString()
        )
        Fitness.getHistoryClient(context, account).readData(readRequest)
            .addOnSuccessListener { dataReadResponse: DataReadResponse ->
//                resultVariable = dataReadResponse.dataSets[0].dataPoints.firstOrNull()
//                    ?.getValue(Field.FIELD_STEPS)?.toString()
                val totalSteps = dataReadResponse.buckets
                    .flatMap { it.dataSets }
                    .flatMap { it.dataPoints }
                    .sumBy { it.getValue(Field.FIELD_STEPS).asInt() }
                Log.i("SUM", "Total steps: $totalSteps")
                dataReadResponse.buckets.forEach {bu ->
                    bu.dataSets.forEach { dt ->
                        dt.dataPoints.forEach{ dp ->
                            Log.d(
                                "DATA",
                                dp.getValue(Field.FIELD_STEPS).toString()
                            )
                        }
                    }
                }
            }
            .addOnFailureListener { dataReadResponse: Exception ->
                Log.d(
                    "TAG",
                    dataReadResponse.message!!
                )
            }
        platformInterface.sendPluginResult(resultVariable)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun enableBackgroundJob() {

        val intent = Intent(context, MyDataUpdateService::class.java)
        val pendingIntent =
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        //SensorClien

        val dataSourceStep = DataSource.Builder()
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            //.setType(DataSource.T)
            .build()

        Fitness.getRecordingClient(
            context,
            GoogleSignIn.getAccountForExtension(context, fitnessOptions)
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

        Fitness.getSensorsClient(activity, account!!)
            .add(
                SensorRequest.Builder()
                    .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                    .setSamplingRate(1, TimeUnit.MINUTES) // sample once per minute
                    .build()
            ) {
                Toast.makeText(context, "UPDATE TYPE_STEP_COUNT_DELTA1", Toast.LENGTH_SHORT)
                    .show()
                Log.i("OnDataPointListener:", it.toString())
            }
            .addOnSuccessListener {
                Log.i("Access GoogleFit:", "SensorRequest")
            }

        Fitness.getSensorsClient(activity, account!!)
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
            context,
            GoogleSignIn.getAccountForExtension(context, fitnessOptions)
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
            context,
            GoogleSignIn.getAccountForExtension(context, fitnessOptions)
        )
            .registerDataUpdateListener(request)
            .addOnSuccessListener {
                Log.i("Access GoogleFit:", "DataUpdateListener registered")
            }
    }


}