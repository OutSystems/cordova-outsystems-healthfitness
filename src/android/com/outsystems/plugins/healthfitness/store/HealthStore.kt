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
import com.google.gson.annotations.SerializedName
import java.lang.Integer.max
import java.lang.Integer.min
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.WeekFields
import java.util.*
import com.outsystems.plugins.healthfitness.HealthFitnessError
import java.time.LocalDateTime
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.outsystems.plugins.healthfitness.AndroidPlatformInterface
import com.outsystems.plugins.healthfitness.MyDataUpdateService
import com.outsystems.plugins.healthfitness.OSHealthFitness
import org.json.JSONArray
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit


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
enum class EnumOperationType(val value : String){
    SUM("SUM"),
    MIN("MIN"),
    MAX("MAX"),
    AVERAGE("AVERAGE"),
    RAW("RAW"),
}
enum class EnumTimeUnit(val value : TimeUnit) {
    MILLISECOND(TimeUnit.MILLISECONDS),
    SECOND(TimeUnit.SECONDS),
    MINUTE(TimeUnit.MINUTES),
    HOUR(TimeUnit.HOURS),
    DAY(TimeUnit.DAYS),
    WEEK(TimeUnit.DAYS),
    MONTH(TimeUnit.DAYS),
    YEAR(TimeUnit.DAYS)
}

class HealthStore(val platformInterface: AndroidPlatformInterface) {
    var context: Context = platformInterface.getContext()
    var activity: Activity = platformInterface.getActivity()

    private var fitnessOptions: FitnessOptions? = null
    private var account: GoogleSignInAccount? = null
    private val gson: Gson by lazy { Gson() }

    private val fitnessVariablesMap: Map<String, GoogleFitVariable> by lazy {
        mapOf(
            "STEPS" to GoogleFitVariable(DataType.TYPE_STEP_COUNT_DELTA, listOf(
                Field.FIELD_STEPS
            )),
            "CALORIES_BURNED" to GoogleFitVariable(DataType.TYPE_CALORIES_EXPENDED, listOf(
                Field.FIELD_CALORIES
            )),
            "PUSH_COUNT" to GoogleFitVariable(DataType.TYPE_ACTIVITY_SEGMENT, listOf(
                //TODO:
            )),
            "MOVE_MINUTES" to GoogleFitVariable(DataType.TYPE_MOVE_MINUTES, listOf(
                Field.FIELD_DURATION
            ))
        )
    }
    private val healthVariablesMap: Map<String, GoogleFitVariable> by lazy {
        mapOf(
            "HEART_RATE" to GoogleFitVariable(DataType.TYPE_HEART_RATE_BPM, listOf(
                Field.FIELD_BPM
            )),
            "SLEEP" to GoogleFitVariable(DataType.TYPE_SLEEP_SEGMENT, listOf(
                Field.FIELD_SLEEP_SEGMENT_TYPE
            )),
            "BLOOD_GLUCOSE" to GoogleFitVariable(HealthDataTypes.TYPE_BLOOD_GLUCOSE, listOf(
                HealthFields.FIELD_BLOOD_GLUCOSE_LEVEL
            )),
            "BLOOD_PRESSURE" to GoogleFitVariable(HealthDataTypes.TYPE_BLOOD_PRESSURE, listOf(
                HealthFields.FIELD_BLOOD_PRESSURE_SYSTOLIC,
                HealthFields.FIELD_BLOOD_PRESSURE_DIASTOLIC
            )),
            "HYDRATION" to GoogleFitVariable(DataType.TYPE_HYDRATION, listOf(
                Field.FIELD_VOLUME
            )),
            "NUTRITION" to GoogleFitVariable(DataType.TYPE_NUTRITION, listOf(
                //TODO:
            ))
        )
    }
    private val profileVariablesMap: Map<String, GoogleFitVariable> by lazy {
        mapOf(
            "HEIGHT" to GoogleFitVariable(DataType.TYPE_HEIGHT, listOf(
                Field.FIELD_HEIGHT
            )),
            "WEIGHT" to GoogleFitVariable(DataType.TYPE_WEIGHT, listOf(
                Field.FIELD_WEIGHT
            )),
            "BODY_FAT_PERCENTAGE" to GoogleFitVariable(DataType.TYPE_BODY_FAT_PERCENTAGE, listOf(
                Field.FIELD_PERCENTAGE
            )),
            "BASAL_METABOLIC_RATE" to GoogleFitVariable(DataType.TYPE_BASAL_METABOLIC_RATE, listOf(
                Field.FIELD_CALORIES
            ))
        )
    }
    private val summaryVariablesMap: Map<String, GoogleFitVariable> by lazy {
        mapOf(
            "HEIGHT_SUMMARY" to GoogleFitVariable(DataType.AGGREGATE_HEIGHT_SUMMARY, listOf()),
            "WEIGHT_SUMMARY" to GoogleFitVariable(DataType.AGGREGATE_WEIGHT_SUMMARY, listOf())
        )
    }

    private val timeUnitsForMinMaxAverage: Map<String, EnumTimeUnit> by lazy {
        mapOf(
            "MILLISECONDS" to EnumTimeUnit.MILLISECOND,
            "SECONDS" to EnumTimeUnit.MILLISECOND,
            "MINUTE" to EnumTimeUnit.SECOND,
            "HOUR" to EnumTimeUnit.MINUTE,
            "DAY" to EnumTimeUnit.HOUR,
            "WEEK" to EnumTimeUnit.DAY,
            "MONTH" to EnumTimeUnit.WEEK,
            "YEAR" to EnumTimeUnit.MONTH //TODO: This doesn't work yet
        )
    }
    private val timeUnits: Map<String, EnumTimeUnit> by lazy {
        mapOf(
            "MILLISECONDS" to EnumTimeUnit.MILLISECOND,
            "SECONDS" to EnumTimeUnit.SECOND,
            "MINUTE" to EnumTimeUnit.MINUTE,
            "HOUR" to EnumTimeUnit.HOUR,
            "DAY" to EnumTimeUnit.DAY,
            "WEEK" to EnumTimeUnit.WEEK,
            "MONTH" to EnumTimeUnit.MONTH,
            "YEAR" to EnumTimeUnit.YEAR
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
    fun updateData(variable: String, value: Float) {

        //right now we are only writing data which are float values

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
                .setField(fieldType, value)
                .build()

        val dataSet = DataSet.builder(dataSourceWrite)
                .add(valueToWrite)
                .build()

        val lastAccount = GoogleSignIn.getLastSignedInAccount(context)

        Fitness.getHistoryClient(
                activity,
                lastAccount
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

        val endDate: Long = Date().time
        val month = 2592000000
        val startDate: Long = endDate - month

        val advancedQueryParameters = AdvancedQueryParameters(
            variable,
            Date.from(Instant.ofEpochMilli(startDate)),
            Date.from(Instant.ofEpochMilli(endDate)),
            null
        )
        advancedQuery(advancedQueryParameters)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun advancedQuery(parameters : AdvancedQueryParameters) {

        val googleFitVariable = getVariableByName(parameters.variable)

        googleFitVariable?.let { variable ->

            val endTime = parameters.endDate
            val startTime = parameters.startDate

            var operationType : String? = null
            parameters.operationType?.let{
                operationType = parameters.operationType
            }

            val requestBuilder = DataReadRequest.Builder()
                .setTimeRange(startTime.time, endTime.time, TimeUnit.MILLISECONDS)

            if(variable.dataType == DataType.TYPE_STEP_COUNT_DELTA) {
                //This is the special case for step count
                val datasource = DataSource.Builder()
                    .setAppPackageName("com.google.android.gms")
                    .setDataType(variable.dataType)
                    .setType(DataSource.TYPE_DERIVED)
                    .setStreamName("estimated_steps")
                    .build()

                //TODO: Needs refactoring
                if(operationType == EnumOperationType.RAW.value){
                    requestBuilder.read(datasource)
                }
                else {
                    requestBuilder.aggregate(datasource)
                }
            }
            else {
                //TODO: Needs refactoring
                if(operationType == EnumOperationType.RAW.value){
                    requestBuilder.read(variable.dataType)
                }
                else {
                    requestBuilder.aggregate(variable.dataType)
                }
            }

            var timeUnitLength : Int? = null
            if(parameters.timeUnit != null) {
                val timeUnit = if(operationType == EnumOperationType.SUM.value || operationType == EnumOperationType.RAW.value) {
                    timeUnits.getOrDefault(parameters.timeUnit, EnumTimeUnit.DAY)
                } else {
                    timeUnitsForMinMaxAverage.getOrDefault(parameters.timeUnit, EnumTimeUnit.DAY)
                }

                parameters.timeUnitLength?.let{
                    timeUnitLength = max(1, it)
                    //TODO: Needs refactoring
                    if(parameters.timeUnit == "WEEK" ||
                        parameters.timeUnit == "MONTH"||
                        parameters.timeUnit == "YEAR") {
                        requestBuilder.bucketByTime(1, timeUnit.value)
                    }
                    else {
                        requestBuilder.bucketByTime(timeUnitLength!!, timeUnit.value)
                    }
                }
            }
            else {
                requestBuilder.setLimit(1)
            }

            val lastAccount = GoogleSignIn.getLastSignedInAccount(context)

            val fitnessRequest = requestBuilder.build()
            Fitness.getHistoryClient(context, lastAccount)
                .readData(fitnessRequest)
                .addOnSuccessListener { dataReadResponse: DataReadResponse ->

                    var processedBuckets = listOf<ProcessedBucket>()

                    timeUnitLength?.let {
                        //TODO: Needs refactoring
                        when(parameters.timeUnit) {
                            "WEEK" -> {
                                processedBuckets = processIntoBucketPerWeek(
                                    dataReadResponse.buckets,
                                    it,
                                    startTime.time,
                                    endTime.time)
                            }
                            "MONTH" -> {
                                processedBuckets = processIntoBucketPerMonth(
                                    dataReadResponse.buckets,
                                    it,
                                    startTime.time,
                                    endTime.time)
                            }
                            else -> {
                                processedBuckets = processBucket(dataReadResponse.buckets)
                            }
                        }
                    }

                    if(timeUnitLength == null) {
                        val values = mutableListOf<Float>()

                        variable.fields.forEach { field ->
                            dataReadResponse.dataSets
                                .flatMap { it.dataPoints }
                                .forEach { dataPoint ->
                                    values.add(dataPoint.getValue(field).toString().toFloat())
                                }
                        }

                        val responseBlock = AdvancedQueryResponseBlock(
                            0,
                            startTime.time / 1000,
                            endTime.time / 1000,
                            values
                        )
                        val queryResponse = AdvancedQueryResponse(listOf(responseBlock))
                        val pluginResponseJson = gson.toJson(queryResponse)
                        Log.d("STORE", "Response $pluginResponseJson")
                        platformInterface.sendPluginResult(pluginResponseJson)
                    }
                    else {
                        val resultBuckets = processBucketOperation(processedBuckets, variable, operationType)
                        val queryResponse = buildAdvancedQueryResult(resultBuckets)
                        val pluginResponseJson = gson.toJson(queryResponse)
                        Log.d("STORE", "Response $pluginResponseJson")
                        platformInterface.sendPluginResult(pluginResponseJson)
                    }
                }
                .addOnFailureListener { dataReadResponse: Exception ->
                    Log.d("STORE", dataReadResponse.message!!)
                }
        }
    }

    private fun buildAdvancedQueryResult(resultBuckets : List<ProcessedBucket>) : AdvancedQueryResponse {

        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        var block = 0
        val blockList : MutableList<AdvancedQueryResponseBlock> = mutableListOf()
        for(bucket in resultBuckets) {
            blockList.add(
                AdvancedQueryResponseBlock(
                    block,
                    bucket.startDate / 1000,
                    bucket.endDate / 1000,
                    bucket.processedDataPoints
                )
            )
            block++
        }
        var result = AdvancedQueryResponse(blockList)
        return result
    }

    data class ProcessedBucket(
        val startDate : Long,
        var endDate : Long,
        var dataPoints : MutableList<DataPoint>,
        var processedDataPoints : MutableList<Float>,
        var DEBUG_startDate : String,
        var DEBUG_endDate : String
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processBucket(bucketsPerDay : List<Bucket>) : List<ProcessedBucket> {

        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        val processedBuckets : MutableList<ProcessedBucket> = mutableListOf()

        for(bucket in bucketsPerDay) {

            val dataPointsPerBucket = bucket.dataSets.flatMap { it.dataPoints }
            if(dataPointsPerBucket.isEmpty()){ continue }

            val startDate = bucket.getStartTime(TimeUnit.MILLISECONDS)
            val endDate = bucket.getEndTime(TimeUnit.MILLISECONDS)
            val processedBucket = ProcessedBucket(
                startDate,
                endDate,
                mutableListOf(),
                mutableListOf(),
                format.format(startDate),
                format.format(endDate)
            )

            dataPointsPerBucket.forEach { dataPoint ->
                processedBucket.dataPoints.add(dataPoint)
            }

            processedBuckets.add(processedBucket)
//                Log.d("POINT",
//                    "$monthNumber -> ${format.format(dataPoint.getStartTime(TimeUnit.MILLISECONDS))} : ${dataPoint.getValue(Field.FIELD_CALORIES)}")
        }
        return processedBuckets
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processIntoBucketPerWeek(bucketsPerDay : List<Bucket>,
                                         timeUnitLength : Int,
                                         queryStartDate : Long,
                                         queryEndDate : Long) : List<ProcessedBucket> {

        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        val processedBuckets : MutableMap<String, ProcessedBucket> = mutableMapOf()
        val weekKeyQueue : ArrayDeque<String> = ArrayDeque()

        //Merge buckets into weeks
        for(bucket in bucketsPerDay) {

            val dataPointsPerBucket = bucket.dataSets.flatMap { it.dataPoints }

            if(dataPointsPerBucket.isEmpty()){ continue }

            dataPointsPerBucket.forEach { dataPoint ->

                val dataPointDate = Instant
                    .ofEpochMilli(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                val weekFields: WeekFields = WeekFields.ISO
                val weekNumber = dataPointDate.get(weekFields.weekOfWeekBasedYear())
                val yearNumber = dataPointDate.year
                val dataPointKey = "$weekNumber$yearNumber"

                if(!processedBuckets.containsKey(dataPointKey)) {

                    val c = Calendar.getInstance()
                    c.set(Calendar.WEEK_OF_YEAR, weekNumber)

                    val firstDayOfWeek = c.firstDayOfWeek

                    c[Calendar.DAY_OF_WEEK] = firstDayOfWeek
                    var startDate = c.timeInMillis
                    if(startDate < queryStartDate) { startDate = queryStartDate }

                    c[Calendar.DAY_OF_WEEK] = firstDayOfWeek + 6
                    var endDate = c.timeInMillis
                    if(endDate > queryEndDate) { endDate = queryEndDate }

                    weekKeyQueue.add(dataPointKey)

                    processedBuckets[dataPointKey] = ProcessedBucket(
                        startDate,
                        endDate,
                        mutableListOf(),
                        mutableListOf(),
                        format.format(startDate),
                        format.format(endDate)
                    )
                }

//                Log.d("POINT",
//                    "$weekNumber -> ${format.format(dataPoint.getStartTime(TimeUnit.MILLISECONDS))} : ${dataPoint.getValue(Field.FIELD_CALORIES)}")

                processedBuckets[dataPointKey]!!.dataPoints.add(dataPoint)

            }

        }

        //TODO: Needs refactoring
        //Merge buckets into groups
        while(!weekKeyQueue.isEmpty()) {
            val keyAnchor = weekKeyQueue.pop()

            for(i in 1 until min(timeUnitLength, weekKeyQueue.size + 1)) {

                val keyToMergeWithAnchor = weekKeyQueue.pop()
                val valuesToMerge = processedBuckets[keyToMergeWithAnchor]!!

                processedBuckets[keyAnchor]!!.dataPoints.addAll(valuesToMerge.dataPoints)
                processedBuckets[keyAnchor]!!.endDate = valuesToMerge.endDate

                processedBuckets.remove(keyToMergeWithAnchor)
            }
        }

        return processedBuckets.values.toList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processIntoBucketPerMonth(bucketsPerDay : List<Bucket>,
                                         timeUnitLength : Int,
                                         queryStartDate : Long,
                                         queryEndDate : Long) : List<ProcessedBucket> {

        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        val processedBuckets : MutableMap<String, ProcessedBucket> = mutableMapOf()
        val bucketKeyQueue : ArrayDeque<String> = ArrayDeque()

        //Merge buckets into Months
        for(bucket in bucketsPerDay) {

            val dataPointsPerBucket = bucket.dataSets.flatMap { it.dataPoints }
            if(dataPointsPerBucket.isEmpty()){ continue }

            dataPointsPerBucket.forEach { dataPoint ->

                val dataPointDate = Instant
                    .ofEpochMilli(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                val monthNumber = dataPointDate.monthValue
                val yearNumber = dataPointDate.year
                val dataPointKey = "$monthNumber$yearNumber"

                if(!processedBuckets.containsKey(dataPointKey)) {

                    val c = Calendar.getInstance()
                    c.set(Calendar.MONTH, monthNumber)

                    val firstDayOfWeek = c.firstDayOfWeek

                    c[Calendar.DAY_OF_WEEK] = firstDayOfWeek
                    var startDate = c.timeInMillis
                    if(startDate < queryStartDate) { startDate = queryStartDate }

                    c[Calendar.DAY_OF_WEEK] = firstDayOfWeek + 6
                    var endDate = c.timeInMillis
                    if(endDate > queryEndDate) { endDate = queryEndDate }

                    processedBuckets[dataPointKey] = ProcessedBucket(
                        startDate,
                        endDate,
                        mutableListOf(),
                        mutableListOf(),
                        format.format(startDate),
                        format.format(endDate)
                    )
                }

//                Log.d("POINT",
//                    "$monthNumber -> ${format.format(dataPoint.getStartTime(TimeUnit.MILLISECONDS))} : ${dataPoint.getValue(Field.FIELD_CALORIES)}")

                processedBuckets[dataPointKey]!!.dataPoints.add(dataPoint)
            }
        }

        //TODO: Needs refactoring
        //Merge buckets into groups
        while(!bucketKeyQueue.isEmpty()) {
            val keyAnchor = bucketKeyQueue.pop()

            for(i in 1 until min(timeUnitLength, bucketKeyQueue.size + 1)) {

                val keyToMergeWithAnchor = bucketKeyQueue.pop()
                val valuesToMerge = processedBuckets[keyToMergeWithAnchor]!!

                processedBuckets[keyAnchor]!!.dataPoints.addAll(valuesToMerge.dataPoints)
                processedBuckets[keyAnchor]!!.endDate = valuesToMerge.endDate

                processedBuckets.remove(keyToMergeWithAnchor)
            }
        }

        return processedBuckets.values.toList()
    }


    private fun processBucketOperation(buckets : List<ProcessedBucket>,
                                       variable : GoogleFitVariable,
                                       inputOperationType : String?) : List<ProcessedBucket> {

        var operationType = EnumOperationType.RAW.value
        if(inputOperationType != null) {
            operationType = inputOperationType
        }

        for(bucket in buckets) {

            val resultPerField : MutableMap<String, MutableList<Float>> = mutableMapOf()
            var nResultsPerField = 0

            for(datePoint in bucket.dataPoints) {
                variable.fields.forEach { field ->

                    if(!resultPerField.containsKey(field.name)) {
                        val results = mutableListOf<Float>()
                        resultPerField[field.name] = results
                        if(operationType != EnumOperationType.RAW.value) {
                            results.add(0F)
                        }
                    }

                    val dataPointValue = datePoint.getValue(field).toString().toFloat()

                    when(operationType) {

                        EnumOperationType.RAW.value -> {
                            nResultsPerField += 1
                            resultPerField[field.name]!!.add(dataPointValue)
                        }

                        EnumOperationType.SUM.value -> {
                            nResultsPerField = 1
                            resultPerField[field.name]!![0] =
                                resultPerField[field.name]!![0] + dataPointValue
                        }

                        EnumOperationType.MAX.value -> {
                            nResultsPerField = 1
                            var maxValue = resultPerField[field.name]!![0]
                            if(maxValue < dataPointValue) { maxValue = dataPointValue }
                            resultPerField[field.name]!![0] = maxValue
                        }

                        EnumOperationType.MIN.value -> {
                            nResultsPerField = 1
                            var minValue = resultPerField[field.name]!![0]
                            if(minValue > dataPointValue) { minValue = dataPointValue }
                            resultPerField[field.name]!![0] = minValue
                        }

                        EnumOperationType.AVERAGE.value -> {
                            //TODO: Implement this operation
                        }

                    }

                }
            }

            nResultsPerField /= variable.fields.size
            for(variableResultIndex in 0 until nResultsPerField) {
                variable.fields.forEach { field ->
                    bucket.processedDataPoints.add(resultPerField[field.name]!![variableResultIndex])
                }
            }

        }

        return buckets
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