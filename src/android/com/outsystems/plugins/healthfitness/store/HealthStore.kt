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
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.DataUpdateListenerRegistrationRequest
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.gson.Gson
import com.outsystems.plugins.healthfitness.AndroidPlatformInterface
import com.outsystems.plugins.healthfitness.HealthFitnessError
import com.outsystems.plugins.healthfitness.OSHealthFitness
import com.outsystems.plugins.healthfitness.MyDataUpdateService
import org.json.JSONArray
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import java.util.*


enum class EnumAccessType(val value : String) {
    READ("READ"),
    WRITE("WRITE"),
    READWRITE("READWRITE")
}
enum class EnumVariableGroup(val value : String) {
    FITNESS("FITNESS"),
    HEALTH("HEALTH"),
    PROFILE("PROFILE"),
    SUMMARY("SUMMARY")
}

class  HealthStore(val platformInterface: AndroidPlatformInterface) {
    var context: Context = platformInterface.getContext()
    var activity: Activity = platformInterface.getActivity()

    private var fitnessOptions: FitnessOptions? = null
    private var account: GoogleSignInAccount? = null
    private val gson: Gson by lazy { Gson() }

    private val fitnessVariablesMap: Map<String, GoogleFitVariable> by lazy {
        mapOf(
            "STEPS" to GoogleFitVariable(DataType.TYPE_STEP_COUNT_DELTA, listOf(Field.FIELD_STEPS)),
            "CALORIES_BURNED" to GoogleFitVariable(DataType.TYPE_CALORIES_EXPENDED, listOf(Field.FIELD_CALORIES))
        )
    }
    private val healthVariablesMap: Map<String, GoogleFitVariable> by lazy {
        mapOf(
            "HEART_RATE" to GoogleFitVariable(DataType.TYPE_HEART_RATE_BPM, listOf(Field.FIELD_BPM)),
            "SLEEP" to GoogleFitVariable(DataType.TYPE_SLEEP_SEGMENT, listOf(Field.FIELD_SLEEP_SEGMENT_TYPE))
        )
    }
    private val profileVariablesMap: Map<String, GoogleFitVariable> by lazy {
        mapOf(
            "HEIGHT" to GoogleFitVariable(DataType.TYPE_HEIGHT, listOf(Field.FIELD_HEIGHT)),
            "WEIGHT" to GoogleFitVariable(DataType.TYPE_WEIGHT, listOf(Field.FIELD_WEIGHT)),
            "BODY_FAT_PERCENTAGE" to GoogleFitVariable(DataType.TYPE_BODY_FAT_PERCENTAGE, listOf(Field.FIELD_PERCENTAGE)),
            "BASAL_METABOLIC_RATE" to GoogleFitVariable(DataType.TYPE_BASAL_METABOLIC_RATE, listOf(Field.FIELD_CALORIES))
        )
    }
    private val summaryVariablesMap: Map<String, GoogleFitVariable> by lazy {
        mapOf(
            "HEIGHT_SUMMARY" to GoogleFitVariable(DataType.AGGREGATE_HEIGHT_SUMMARY, listOf(Field.FIELD_HEIGHT)),
            "WEIGHT_SUMMARY" to GoogleFitVariable(DataType.AGGREGATE_WEIGHT_SUMMARY, listOf(Field.FIELD_WEIGHT))
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initAndRequestPermissions(args: JSONArray) {

        val customPermissions = args.getString(0)
        val allVariables = args.getString(1)
        val fitnessVariables = args.getString(2)
        val healthVariables = args.getString(3)
        val profileVariables = args.getString(4)
        val summaryVariables = args.getString(5)

        var permissionList: MutableList<Pair<DataType, Int>> = mutableListOf()
        val allVariablesPermissions = gson.fromJson(allVariables, GoogleFitGroupPermission::class.java)

        if(allVariablesPermissions.isActive){
            permissionList = parseAllVariablesPermissions(allVariablesPermissions)
        }
        else {
            val fitnessVariablesPermissions = gson.fromJson(fitnessVariables, GoogleFitGroupPermission::class.java)
            val healthVariablesPermissions = gson.fromJson(healthVariables, GoogleFitGroupPermission::class.java)
            val profileVariablesPermissions = gson.fromJson(profileVariables, GoogleFitGroupPermission::class.java)
            val summaryVariablesPermissions = gson.fromJson(summaryVariables, GoogleFitGroupPermission::class.java)

            if(fitnessVariablesPermissions.isActive){
                appendPermissions(fitnessVariablesPermissions, permissionList, EnumVariableGroup.FITNESS)
            }
            if(healthVariablesPermissions.isActive){
                appendPermissions(healthVariablesPermissions, permissionList, EnumVariableGroup.HEALTH)
            }
            if(profileVariablesPermissions.isActive){
                appendPermissions(profileVariablesPermissions, permissionList, EnumVariableGroup.PROFILE)
            }
            if(summaryVariablesPermissions.isActive){
                appendPermissions(summaryVariablesPermissions, permissionList, EnumVariableGroup.SUMMARY)
            }
            permissionList.addAll(parseCustomPermissions(customPermissions, permissionList))
        }
        initFitnessOptions(permissionList)
    }

    private fun appendPermissions(permission: GoogleFitGroupPermission?, permissionList: MutableList<Pair<DataType, Int>>, variableGroup: EnumVariableGroup) {
        if(variableGroup == EnumVariableGroup.FITNESS){
            fitnessVariablesMap.forEach{ variable ->
                processAccessType(variable, permissionList, permission)
            }
        }
        else if(variableGroup == EnumVariableGroup.HEALTH){
            healthVariablesMap.forEach{ variable ->
                processAccessType(variable, permissionList, permission)
            }
        }
        else if(variableGroup == EnumVariableGroup.PROFILE){
            profileVariablesMap.forEach{ variable ->
                processAccessType(variable, permissionList, permission)
            }
        }
        else{
            summaryVariablesMap.forEach{ variable ->
                processAccessType(variable, permissionList, permission)
            }
        }
    }

    private fun processAccessType(variable: Map.Entry<String, GoogleFitVariable>, permissionList: MutableList<Pair<DataType, Int>>, permission: GoogleFitGroupPermission?) {
        if(permission?.accessType == EnumAccessType.WRITE.value){
            permissionList.add(Pair(variable.value.dataType, FitnessOptions.ACCESS_WRITE))
        }
        else if(permission?.accessType == EnumAccessType.READWRITE.value){
            permissionList.add(Pair(variable.value.dataType, FitnessOptions.ACCESS_READ))
            permissionList.add(Pair(variable.value.dataType, FitnessOptions.ACCESS_WRITE))
        }
        else{
            permissionList.add(Pair(variable.value.dataType, FitnessOptions.ACCESS_READ))
        }
    }

    private fun parseAllVariablesPermissions(allVariablesPermissions: GoogleFitGroupPermission?): MutableList<Pair<DataType, Int>> {
        val result: MutableList<Pair<DataType, Int>> = mutableListOf()
        allVariablesPermissions?.let {
            fitnessVariablesMap.forEach { variable ->
                processAccessType(variable, result, it)
            }
            healthVariablesMap.forEach { variable ->
                processAccessType(variable, result, it)
            }
            profileVariablesMap.forEach { variable ->
                processAccessType(variable, result, it)
            }
            summaryVariablesMap.forEach { variable ->
                processAccessType(variable, result, it)
            }
        }
        return result
    }

    private fun parseCustomPermissions(permissionsJson : String, permissionList: List<Pair<DataType, Int>>) : List<Pair<DataType, Int>> {
        val result: MutableList<Pair<DataType, Int>> = mutableListOf()
        val permissions = gson.fromJson(permissionsJson, Array<GoogleFitPermission>::class.java)

        permissions.forEach { permission ->
            val googleVariable = getVariableByName(permission.variable)
            googleVariable?.let { it ->
                if(permission.accessType == EnumAccessType.WRITE.value) {
                    result.add(Pair(it.dataType, FitnessOptions.ACCESS_WRITE))
                }
                else if(permission.accessType == EnumAccessType.READWRITE.value){
                    result.add(Pair(it.dataType, FitnessOptions.ACCESS_READ))
                    result.add(Pair(it.dataType, FitnessOptions.ACCESS_WRITE))
                }
                else {
                    result.add(Pair(it.dataType, FitnessOptions.ACCESS_READ))
                }
            }
        }

        return result
    }

    private fun getVariableByName(name : String) : GoogleFitVariable? {
        return if(fitnessVariablesMap.containsKey(name)){
            fitnessVariablesMap[name]
        } else if(healthVariablesMap.containsKey(name)){
            healthVariablesMap[name]
        } else if(profileVariablesMap.containsKey(name)){
            profileVariablesMap[name]
        } else if(summaryVariablesMap.containsKey(name)){
            summaryVariablesMap[name]
        } else {
            null
        }
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
        if(!checkAllGoogleFitPermissionGranted()){
            fitnessOptions?.let {
                GoogleSignIn.requestPermissions(
                    platformInterface.getActivity(),  // your activity
                    OSHealthFitness.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,  // e.g. 1
                    account,
                    it
                )
            }
            //instead of calling sendPluginResult here we should call it in the onActivityResult after the user provides the permissions in the permission dialog
            //but the onActivityResult is not being called for some reason
            platformInterface.sendPluginResult("success", null)
        }
        else{
            platformInterface.sendPluginResult("success", null)
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
    fun updateData(variable: String, value: String) {

        //right now we are only writing data which are float values
        val convertedValue = value.toFloat()

        val dataType = profileVariablesMap[variable]?.dataType

        //profile variables only have 1 field each, so it is safe to get the first entry of the fields list
        val fieldType = profileVariablesMap[variable]?.fields?.get(0)

        //insert the data
        val dataSourceWrite = DataSource.Builder()
            .setAppPackageName(context)
            .setDataType(dataType)
            .setType(DataSource.TYPE_RAW)
            .build()

        val timestamp = System.currentTimeMillis()
        val valueToWrite = DataPoint.builder(dataSourceWrite)
            .setTimestamp(timestamp, TimeUnit.MILLISECONDS)
            .setField(fieldType, convertedValue)
            .build()

        val dataSet = DataSet.builder(dataSourceWrite)
            .add(valueToWrite)
            .build()

        Fitness.getHistoryClient(
            activity,
            account
        )
            .insertData(dataSet)
            .addOnSuccessListener {
                Log.i("Access GoogleFit:", "DataSet updated successfully!")
                platformInterface.sendPluginResult("success", null)
            }
            .addOnFailureListener { e ->
                Log.w("Access GoogleFit:", "There was an error updating the DataSet", e)
                //In this case, what is the error we want to send in the callback?
                //We could identify the exception that is thrown and send an error accordingly?
                //Maybe catch that com.google.android.gms.common.api.ApiException: 4: The user must be signed in to make this API call.
                //For now we will send a generic error message
                platformInterface.sendPluginResult(null, Pair(HealthFitnessError.WRITE_DATA_ERROR.code, HealthFitnessError.WRITE_DATA_ERROR.message))
            }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getLastRecord(variable: String) {

        val endTime: Long = Date().time
        val month = 2592000000
        val startTime: Long = endTime - month

        var dataType : DataType? = null
        var fieldType : Field? = null

        if(fitnessVariablesMap.containsKey(variable)){
            dataType = fitnessVariablesMap[variable]?.dataType
            fieldType = fitnessVariablesMap[variable]?.fields?.get(0)
        }

        else if(healthVariablesMap.containsKey(variable)){
            dataType = healthVariablesMap[variable]?.dataType
            fieldType = healthVariablesMap[variable]?.fields?.get(0)
        }

        else if(profileVariablesMap.containsKey(variable)){
            dataType = profileVariablesMap[variable]?.dataType
            fieldType = profileVariablesMap[variable]?.fields?.get(0)
        }

        val readRequest = DataReadRequest.Builder()
            .read(dataType)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .setLimit(1)
            .build()

        Fitness.getHistoryClient(
            activity, account)
            .readData(readRequest)
            .addOnSuccessListener{ dataReadResponse ->
                var value : Float? = null
                for (dataSet in dataReadResponse.dataSets) {
                    for (dataPoint in dataSet.dataPoints) {
                        value = dataPoint.getValue(fieldType).toString().toFloat()
                        Log.w("DATA POINT IS", dataPoint.getValue(fieldType).toString());
                    }
                }
                platformInterface.sendPluginResult(value, null)
            }
            .addOnFailureListener { e ->
                Log.w("Access GoogleFit:", "There was an error updating the DataSet", e)
                platformInterface.sendPluginResult(null, Pair(HealthFitnessError.WRITE_DATA_ERROR.code, HealthFitnessError.WRITE_DATA_ERROR.message))
            }
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
    fun getData(args : JSONArray) {

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