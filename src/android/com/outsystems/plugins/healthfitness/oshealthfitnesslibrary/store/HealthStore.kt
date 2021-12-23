package com.outsystems.plugins.healthfitness.store

import android.app.Activity
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.util.Log
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.gson.Gson
import com.outsystems.plugins.healthfitness.HealthFitnessError
import com.outsystems.plugins.healthfitness.background.BackgroundJobsResponse
import com.outsystems.plugins.healthfitness.background.BackgroundJobsResponseBlock
import com.outsystems.plugins.healthfitness.background.UpdateBackgroundJobParameters
import com.outsystems.plugins.healthfitness.background.BackgroundJobParameters
import com.outsystems.plugins.healthfitness.background.database.BackgroundJob
import com.outsystems.plugins.healthfitness.background.database.DatabaseManagerInterface
import com.outsystems.plugins.healthfitness.background.database.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.lang.NullPointerException
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
enum class EnumTimeUnit(val value : Pair<String, TimeUnit>) {
    MILLISECOND(Pair("MILLISECONDS", TimeUnit.MILLISECONDS)),
    SECOND(Pair("SECONDS", TimeUnit.SECONDS)),
    MINUTE(Pair("MINUTE", TimeUnit.MINUTES)),
    HOUR(Pair("HOUR", TimeUnit.HOURS)),
    DAY(Pair("DAY", TimeUnit.DAYS)),
    WEEK(Pair("WEEK", TimeUnit.DAYS)),
    MONTH(Pair("MONTH", TimeUnit.DAYS)),
    YEAR(Pair("YEAR", TimeUnit.DAYS))
}
enum class EnumJobFrequency(val value : String) {
    IMMEDIATE("IMMEDIATE"),
    HOUR("HOUR"),
    DAY("DAY"),
    WEEK("WEEK")
}

private val jobFrequencies: Map<String, EnumTimeUnit> by lazy {
    mapOf(
        "IMMEDIATE" to EnumTimeUnit.SECOND,
        "HOUR" to EnumTimeUnit.HOUR,
        "DAY" to EnumTimeUnit.DAY,
        "WEEK" to EnumTimeUnit.DAY
    )
}

class HealthStore(
    private val packageName : String,
    private val manager: HealthFitnessManagerInterface,
    private val database : DatabaseManagerInterface): HealthStoreInterface {

    private var fitnessOptions: FitnessOptions? = null
    private val gson: Gson by lazy { Gson() }

    private val historyVariables: Set<String> by lazy {
        setOf(
            "HEIGHT",
            "WEIGHT",
            "BODY_FAT_PERCENTAGE",
            "BASAL_METABOLIC_RATE"
        )
    }
    private val sensorVariables: Set<String> by lazy {
        setOf(
            "STEPS",
            "HEART_RATE",
            "BLOOD_PRESSURE",
            "BLOOD_GLUCOSE",
            "CALORIES_BURNED",
            "SLEEP"
        )
    }
    private val fitnessVariablesMap: Map<String, GoogleFitVariable> by lazy {
        mapOf(
            "STEPS" to GoogleFitVariable(DataType.TYPE_STEP_COUNT_DELTA, listOf(
                Field.FIELD_STEPS
            ),
                listOf(
                    EnumOperationType.RAW.value,
                    EnumOperationType.AVERAGE.value,
                    EnumOperationType.SUM.value,
                    EnumOperationType.MAX.value,
                    EnumOperationType.MIN.value
                )),
            "CALORIES_BURNED" to GoogleFitVariable(DataType.TYPE_CALORIES_EXPENDED, listOf(
                Field.FIELD_CALORIES
            ),
                listOf(
                    EnumOperationType.RAW.value,
                    EnumOperationType.AVERAGE.value,
                    EnumOperationType.SUM.value,
                    EnumOperationType.MAX.value,
                    EnumOperationType.MIN.value
                )),
            "MOVE_MINUTES" to GoogleFitVariable(DataType.TYPE_MOVE_MINUTES, listOf(
                Field.FIELD_DURATION
            ),
                listOf(
                    EnumOperationType.RAW.value,
                    EnumOperationType.AVERAGE.value,
                    EnumOperationType.SUM.value,
                    EnumOperationType.MAX.value,
                    EnumOperationType.MIN.value
                )),
            "WALKING_SPEED" to GoogleFitVariable(DataType.TYPE_SPEED, listOf(
                Field.FIELD_SPEED
            ),
                listOf(
                    EnumOperationType.RAW.value,
                    EnumOperationType.AVERAGE.value,
                    EnumOperationType.MAX.value,
                    EnumOperationType.MIN.value
                ),
                listOf(
                    "walking"
                )
            ),
            "DISTANCE" to GoogleFitVariable(DataType.TYPE_DISTANCE_DELTA, listOf(
                Field.FIELD_DISTANCE
            ),
                listOf(
                    EnumOperationType.RAW.value,
                    EnumOperationType.AVERAGE.value,
                    EnumOperationType.SUM.value,
                    EnumOperationType.MAX.value,
                    EnumOperationType.MIN.value
                ),
                listOf(
                    "walking",
                    "running"
                )
            )
        )
    }
    private val healthVariablesMap: Map<String, GoogleFitVariable> by lazy {
        mapOf(
            "HEART_RATE" to GoogleFitVariable(DataType.TYPE_HEART_RATE_BPM, listOf(
                Field.FIELD_BPM
            ),
                listOf(
                    EnumOperationType.RAW.value
                )),
            "SLEEP" to GoogleFitVariable(DataType.TYPE_SLEEP_SEGMENT, listOf(
                Field.FIELD_SLEEP_SEGMENT_TYPE
            ),
                listOf(
                    EnumOperationType.RAW.value,
                    EnumOperationType.AVERAGE.value,
                    EnumOperationType.MAX.value,
                    EnumOperationType.MIN.value
                )),
            "BLOOD_GLUCOSE" to GoogleFitVariable(HealthDataTypes.TYPE_BLOOD_GLUCOSE, listOf(
                HealthFields.FIELD_BLOOD_GLUCOSE_LEVEL
            ),
                listOf(
                    EnumOperationType.RAW.value,
                    EnumOperationType.AVERAGE.value,
                    EnumOperationType.MAX.value,
                    EnumOperationType.MIN.value
                )),
            "BLOOD_PRESSURE" to GoogleFitVariable(HealthDataTypes.TYPE_BLOOD_PRESSURE, listOf(
                HealthFields.FIELD_BLOOD_PRESSURE_SYSTOLIC,
                HealthFields.FIELD_BLOOD_PRESSURE_DIASTOLIC
            ),
                listOf(
                    EnumOperationType.RAW.value
                )),
            "HYDRATION" to GoogleFitVariable(DataType.TYPE_HYDRATION, listOf(
                Field.FIELD_VOLUME
            ),
                listOf(
                    EnumOperationType.RAW.value,
                    EnumOperationType.SUM.value,
                    EnumOperationType.AVERAGE.value,
                    EnumOperationType.MAX.value,
                    EnumOperationType.MIN.value
                )),
            "NUTRITION" to GoogleFitVariable(DataType.TYPE_NUTRITION, listOf(
                //TODO: possible different from iOS
            ),
                listOf(

                ))
        )
    }
    private val profileVariablesMap: Map<String, GoogleFitVariable> by lazy {
        mapOf(
            "HEIGHT" to GoogleFitVariable(DataType.TYPE_HEIGHT, listOf(
                Field.FIELD_HEIGHT
            ),
                listOf(
                    EnumOperationType.RAW.value,
                    EnumOperationType.AVERAGE.value,
                    EnumOperationType.MAX.value,
                    EnumOperationType.MIN.value
                )),
            "WEIGHT" to GoogleFitVariable(DataType.TYPE_WEIGHT, listOf(
                Field.FIELD_WEIGHT
            ),
                listOf(
                    EnumOperationType.RAW.value,
                    EnumOperationType.AVERAGE.value,
                    EnumOperationType.MAX.value,
                    EnumOperationType.MIN.value
                )),
            "BODY_FAT_PERCENTAGE" to GoogleFitVariable(DataType.TYPE_BODY_FAT_PERCENTAGE, listOf(
                Field.FIELD_PERCENTAGE
            ),
                listOf(
                    EnumOperationType.RAW.value,
                    EnumOperationType.AVERAGE.value,
                    EnumOperationType.MAX.value,
                    EnumOperationType.MIN.value
                )),
            "BASAL_METABOLIC_RATE" to GoogleFitVariable(DataType.TYPE_BASAL_METABOLIC_RATE, listOf(
                Field.FIELD_CALORIES
            ),
                listOf(
                    EnumOperationType.RAW.value,
                    EnumOperationType.AVERAGE.value,
                    EnumOperationType.MAX.value,
                    EnumOperationType.MIN.value
                ))
        )
    }
    private val summaryVariablesMap: Map<String, GoogleFitVariable> by lazy {
        mapOf(
            "HEIGHT_SUMMARY" to GoogleFitVariable(DataType.AGGREGATE_HEIGHT_SUMMARY, listOf(), listOf()),
            "WEIGHT_SUMMARY" to GoogleFitVariable(DataType.AGGREGATE_WEIGHT_SUMMARY, listOf(), listOf())
        )
    }

    override fun getVariableByName(name : String) : GoogleFitVariable? {
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

    override fun initAndRequestPermissions(customPermissions: String,
                                  allVariables: String,
                                  fitnessVariables: String,
                                  healthVariables: String,
                                  profileVariables: String,
                                  summaryVariables: String) {

        var permissionList: MutableList<Pair<DataType, Int>> = mutableListOf()
        val allVariablesPermissions = gson.fromJson(allVariables, GoogleFitGroupPermission::class.java)

        if(allVariablesPermissions.isActive){
            permissionList = createPermissionsForAllVariables(allVariablesPermissions)
        }
        else {
            val fitnessVariablesPermissions = gson.fromJson(fitnessVariables, GoogleFitGroupPermission::class.java)
            val healthVariablesPermissions = gson.fromJson(healthVariables, GoogleFitGroupPermission::class.java)
            val profileVariablesPermissions = gson.fromJson(profileVariables, GoogleFitGroupPermission::class.java)
            val summaryVariablesPermissions = gson.fromJson(summaryVariables, GoogleFitGroupPermission::class.java)

            if(fitnessVariablesPermissions.isActive){
                permissionList.addAll(
                    createPermissionsForVariableGroup(fitnessVariablesPermissions.accessType, EnumVariableGroup.FITNESS))
            }
            if(healthVariablesPermissions.isActive){
                permissionList.addAll(
                    createPermissionsForVariableGroup(healthVariablesPermissions.accessType, EnumVariableGroup.HEALTH))
            }
            if(profileVariablesPermissions.isActive){
                permissionList.addAll(
                    createPermissionsForVariableGroup(profileVariablesPermissions.accessType, EnumVariableGroup.PROFILE))
            }
            if(summaryVariablesPermissions.isActive){
                permissionList.addAll(
                    createPermissionsForVariableGroup(summaryVariablesPermissions.accessType, EnumVariableGroup.SUMMARY))
            }
            permissionList.addAll(createCustomPermissionsForVariables(customPermissions))
        }

        fitnessOptions = createFitnessOptions(permissionList)
        manager.createAccount(fitnessOptions!!)
    }

    private fun createPermissionsForVariableGroup(permission: String,
                                                  variableGroup: EnumVariableGroup) : MutableList<Pair<DataType, Int>>  {

        val permissionList: MutableList<Pair<DataType, Int>> = mutableListOf()
        when(variableGroup) {
            EnumVariableGroup.FITNESS -> {
                fitnessVariablesMap.keys.forEach{ variableKey ->
                    permissionList.addAll(createPermissionsForVariable(fitnessVariablesMap[variableKey]!!, permission))
                }
            }
            EnumVariableGroup.HEALTH -> {
                healthVariablesMap.keys.forEach{ variableKey ->
                    permissionList.addAll(createPermissionsForVariable(healthVariablesMap[variableKey]!!, permission))
                }
            }
            EnumVariableGroup.PROFILE -> {
                profileVariablesMap.keys.forEach{ variableKey ->
                    permissionList.addAll(createPermissionsForVariable(profileVariablesMap[variableKey]!!, permission))
                }
            }
            EnumVariableGroup.SUMMARY -> {
                summaryVariablesMap.keys.forEach{ variableKey ->
                    permissionList.addAll(createPermissionsForVariable(summaryVariablesMap[variableKey]!!, permission))
                }
            }
        }
        return permissionList
    }

    private fun createPermissionsForVariable(variable: GoogleFitVariable,
                                             permission: String) : List<Pair<DataType, Int>> {

        val permissionList = mutableListOf<Pair<DataType, Int>>()
        when(permission) {
            EnumAccessType.WRITE.value -> {
                permissionList.add(Pair(variable.dataType, FitnessOptions.ACCESS_WRITE))
            }
            EnumAccessType.READWRITE.value -> {
                permissionList.add(Pair(variable.dataType, FitnessOptions.ACCESS_READ))
                permissionList.add(Pair(variable.dataType, FitnessOptions.ACCESS_WRITE))
            }
            else -> {
                permissionList.add(Pair(variable.dataType, FitnessOptions.ACCESS_READ))
            }
        }

        return permissionList
    }

    private fun createPermissionsForAllVariables(
        allVariablesPermissions: GoogleFitGroupPermission?): MutableList<Pair<DataType, Int>> {
        val result: MutableList<Pair<DataType, Int>> = mutableListOf()
        allVariablesPermissions?.let {
            fitnessVariablesMap.keys.forEach { variableKey ->
                result.addAll(createPermissionsForVariable(fitnessVariablesMap[variableKey]!!, it.accessType))
            }
            healthVariablesMap.keys.forEach { variableKey ->
                result.addAll(createPermissionsForVariable(healthVariablesMap[variableKey]!!, it.accessType))
            }
            profileVariablesMap.keys.forEach { variableKey ->
                result.addAll(createPermissionsForVariable(profileVariablesMap[variableKey]!!, it.accessType))
            }
            summaryVariablesMap.keys.forEach { variableKey ->
                result.addAll(createPermissionsForVariable(summaryVariablesMap[variableKey]!!, it.accessType))
            }
        }
        return result
    }

    private fun createCustomPermissionsForVariables(permissionsJson : String) : List<Pair<DataType, Int>> {
        val result: MutableList<Pair<DataType, Int>> = mutableListOf()
        val permissions = gson.fromJson(permissionsJson, Array<GoogleFitPermission>::class.java)

        permissions.forEach { permission ->
            val googleVariable = getVariableByName(permission.variable)
                ?: throw HealthStoreException(HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR)

            when(permission.accessType) {
                EnumAccessType.WRITE.value -> {
                    result.add(Pair(googleVariable.dataType, FitnessOptions.ACCESS_WRITE))
                }
                EnumAccessType.READWRITE.value -> {
                    result.add(Pair(googleVariable.dataType, FitnessOptions.ACCESS_READ))
                    result.add(Pair(googleVariable.dataType, FitnessOptions.ACCESS_WRITE))
                }
                else -> {
                    result.add(Pair(googleVariable.dataType, FitnessOptions.ACCESS_READ))
                }
            }
        }

        return result
    }

    private fun createFitnessOptions(permissionList: List<Pair<DataType, Int>>) : FitnessOptions{
        val fitnessBuild = FitnessOptions.builder()
        permissionList.forEach {
            fitnessBuild.addDataType(it.first, it.second)
        }
        return fitnessBuild.build()
    }

    override fun requestGoogleFitPermissions() : Boolean {
        if(manager.areGoogleFitPermissionsGranted(fitnessOptions)){
            return true
        }
        else{
            fitnessOptions?.let {
                manager.requestPermissions(it, GOOGLE_FIT_PERMISSIONS_REQUEST_CODE)
            }
            return false
        }
    }

    override fun handleActivityResult(requestCode: Int,
                             resultCode: Int,
                             intent: Intent) : String? {
        return when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE ->
                        "success"
                    else ->
                        null
                }
            }
            else -> {
                // Permission not granted
                throw HealthStoreException(HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR)
            }
        }
    }

    override fun areGoogleFitPermissionsGranted(): Boolean{
        return manager.areGoogleFitPermissionsGranted(fitnessOptions)
    }

    override fun updateDataAsync(variableName: String,
                        value: Float,
                        onSuccess : (String) -> Unit,
                        onError : (HealthFitnessError) -> Unit) {

        //right now we are only writing data which are float values
        val variable = getVariableByName(variableName)
        if(variable == null) {
            onError(HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR)
            return
        }

        val permissions = createPermissionsForVariable(variable, EnumAccessType.WRITE.value)
        val options = createFitnessOptions(permissions)
        if(!manager.areGoogleFitPermissionsGranted(options)) {
            onError(HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR)
            return
        }

        //profile variables only have 1 field each, so it is safe to get the first entry of the fields list
        val fieldType = profileVariablesMap[variableName]?.fields?.get(0)

        //insert the data
        val dataSourceWrite = DataSource.Builder()
            .setAppPackageName(packageName)
            .setDataType(variable.dataType)
            .setType(DataSource.TYPE_RAW)
            .build()

        val timestamp = System.currentTimeMillis()
        var valueToWrite : DataPoint? = null

        //if the value to write is height, then we need to convert the value to meters
        valueToWrite = if(variableName == "HEIGHT"){
            val convertedValue = value / 100
            DataPoint.builder(dataSourceWrite)
                .setTimestamp(timestamp, TimeUnit.MILLISECONDS)
                .setField(fieldType, convertedValue)
                .build()
        }
        else {
            DataPoint.builder(dataSourceWrite)
                .setTimestamp(timestamp, TimeUnit.MILLISECONDS)
                .setField(fieldType, value)
                .build()
        }

        var dataSet : DataSet? = null

        try {
            dataSet = DataSet.builder(dataSourceWrite)
                .add(valueToWrite)
                .build()
        }
        catch (e : IllegalArgumentException) {
            Log.w("Write to GoogleFit:", "Field out of range", e)
            onError(HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR)
            return
        }

        manager.updateDataOnStore(dataSet,
            {
                Log.i("Access GoogleFit:", "DataSet updated successfully!")
                onSuccess("success")
            },
            { e ->
                Log.w("Access GoogleFit:", "There was an error updating the DataSet", e)
                onError(HealthFitnessError.WRITE_DATA_ERROR)
            }
        )
    }

    override fun getLastRecordAsync(variable: String,
                           onSuccess : (AdvancedQueryResponse) -> Unit,
                           onError : (HealthFitnessError) -> Unit) {

        val endDate: Long = Date().time
        val month = 2592000000
        val startDate: Long = endDate - month

        val advancedQueryParameters = AdvancedQueryParameters(
            variable,
            Date(startDate),
            Date(endDate),
            limit = 1
        )
        advancedQueryAsync(advancedQueryParameters, onSuccess, onError)
    }

    override fun advancedQueryAsync(parameters : AdvancedQueryParameters,
                           onSuccess : (AdvancedQueryResponse) -> Unit,
                           onError : (HealthFitnessError) -> Unit) {

        val variable = getVariableByName(parameters.variable)
        val endDate = parameters.endDate
        val startDate = parameters.startDate

        if(variable == null) {
            onError(HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR)
            return
        }

        if(!variable.allowedOperations.contains(parameters.operationType!!)) {
            onError(HealthFitnessError.OPERATION_NOT_ALLOWED)
            return
        }

        val permissions = createPermissionsForVariable(variable, EnumAccessType.READ.value)
        val options = createFitnessOptions(permissions)
        if(!manager.areGoogleFitPermissionsGranted(options)) {
            onError(HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR)
            return
        }

        val queryInformation = AdvancedQuery(variable, startDate, endDate)
        queryInformation.setOperationType(parameters.operationType)
        queryInformation.setTimeUnit(parameters.timeUnit)
        queryInformation.setTimeUnitGrouping(parameters.timeUnitLength)
        queryInformation.setLimit(parameters.limit)

        manager.getDataFromStore(queryInformation,
            { dataReadResponse ->

                val queryResponse: AdvancedQueryResponse

                if(queryInformation.isSingleResult()) {

                    val values = mutableListOf<Float>()

                    try {
                        variable.fields.forEach { field ->
                            dataReadResponse.dataSets
                                .flatMap { it.dataPoints }
                                .forEach { dataPoint ->
                                    values.add(dataPoint.getValue(field).toString().toFloat())
                                }
                        }
                    }
                    catch (_ : NullPointerException){
                        // Ignores. Should only happen in UnitTesting.
                    }

                    val responseBlock = AdvancedQueryResponseBlock(
                        0,
                        startDate.time / 1000,
                        endDate.time / 1000,
                        values)

                    queryResponse = AdvancedQueryResponse(listOf(responseBlock))
                }
                else {
                    val buckets = try {
                        queryInformation.processBuckets(dataReadResponse.buckets)
                    } catch (_: NullPointerException) {
                        listOf(ProcessedBucket(startDate.time, endDate.time))
                    }

                    queryResponse = buildAdvancedQueryResult(buckets)
                }

                convertResultUnits(parameters.variable, queryResponse)

                Log.d("RESULT", gson.toJson(queryResponse))
                onSuccess(queryResponse)
            },
            { e ->
                onError(HealthFitnessError.READ_DATA_ERROR)
            }
        )

    }

    private fun buildAdvancedQueryResult(resultBuckets: List<ProcessedBucket>): AdvancedQueryResponse {
        val blockList: MutableList<AdvancedQueryResponseBlock> = mutableListOf()
        for ((block, bucket) in resultBuckets.withIndex()) {
            blockList.add(
                AdvancedQueryResponseBlock(
                    block,
                    bucket.startDate / 1000,
                    bucket.endDate / 1000,
                    bucket.processedDataPoints
                )
            )
        }
        return AdvancedQueryResponse(blockList)
    }
    private fun convertResultUnits(variableName : String, response : AdvancedQueryResponse){
        if(variableName == "HEIGHT"){
            response.results.forEach{ bucket ->
                for (i in bucket.values.indices){
                    bucket.values[i] = bucket.values[i] * 100
                }
            }
        }
    }

    override fun setBackgroundJob(parameters: BackgroundJobParameters,
                         onSuccess : (String) -> Unit,
                         onError : (HealthFitnessError) -> Unit) {

        val variable = getVariableByName(parameters.variable)
        if(variable == null) {
            onError(HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR)
            return
        }

        val permissions = createPermissionsForVariable(variable, EnumAccessType.READ.value)
        val options = createFitnessOptions(permissions)
        if(!manager.areGoogleFitPermissionsGranted(options)) {
            onError(HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR)
            return
        }

        manager.subscribeToRecordingUpdates(variable, parameters,
            {
                Log.i("Access GoogleFit:", "Subscribed to Recording Updates with success.")

                runBlocking {
                    launch(Dispatchers.IO) {

                        try {
                            database.runInTransaction {

                                val notification = Notification().apply {
                                    this.title = parameters.notificationHeader
                                    this.body = parameters.notificationBody
                                }
                                val notificationId = database.insert(notification)

                                val backgroundJob = BackgroundJob().apply {
                                    this.variable = parameters.variable
                                    this.comparison = parameters.condition
                                    this.value = parameters.value.toFloat()

                                    this.notificationId = notificationId
                                    this.timeUnit = parameters.timeUnit
                                    this.timeUnitGrouping = parameters.timeUnitGrouping

                                    this.notificationFrequency =
                                        parameters.notificationFrequency.toString()
                                    this.notificationFrequencyGrouping =
                                        parameters.notificationFrequencyGrouping!!

                                    this.nextNotificationTimestamp = System.currentTimeMillis()
                                }
                                database.insert(backgroundJob)
                            }
                            onSuccess("success")
                        } catch(sqle : SQLiteException) {
                            onError(HealthFitnessError.BACKGROUND_JOB_ALREADY_EXISTS_ERROR)
                        } catch(e : Exception) {
                            onError(HealthFitnessError.BACKGROUND_JOB_GENERIC_ERROR)
                        }
                    }
                }
            },
            { exception ->
                Log.w("Access GoogleFit:", "There was a problem subscribing to Recording.", exception)
            }
        )

        if(sensorVariables.contains(parameters.variable)){
            var grouping : Long = 1
            if(parameters.jobFrequency?.equals(EnumJobFrequency.WEEK.value) == true){
                grouping = 7
            }
            val jobFrequency = jobFrequencies[parameters.jobFrequency]?.value?.second
            jobFrequency?.let {
                manager.subscribeToSensorUpdates(variable, parameters.variable, grouping, jobFrequency, parameters,
                    {
                        Log.i("Access GoogleFit:", "Subscribed to Sensor Updates with success.")
                    },
                    {
                        //register the background job in our database calling the insert method
                        Log.w("Access GoogleFit:", "There was a problem subscribing to Sensor Updates.", it)
                    })
            }
        }
        else if(historyVariables.contains(parameters.variable)){
            manager.subscribeToHistoryUpdates(variable, parameters.variable,
                {
                    Log.i("Access GoogleFit:", "Subscribed to History Updates with success.")
                },
                {
                    Log.w("Access GoogleFit:", "There was a problem subscribing to History Updates.", it)
                })
        }
        else {
            //do nothing
            //maybe throw an error because variable is not a sensorVariable nor a historyVariable??
        }
    }

    override fun deleteBackgroundJob(jogId: String,
                            onSuccess : (String) -> Unit,
                            onError : (HealthFitnessError) -> Unit) {

        runBlocking {
            launch(Dispatchers.IO) {

                try{
                    val job = database.fetchBackgroundJob(jogId)
                    if(job != null) {
                        val variableName = job.variable
                        getVariableByName(variableName)?.let { variable ->
                            database.deleteBackgroundJob(job)
                            val jobCount = database.fetchBackgroundJobCountForVariable(variableName)
                            if(jobCount == 0) {
                                manager.unsubscribeFromAllUpdates(
                                    variable,
                                    variableName,
                                    onSuccess = {
                                        onSuccess("success")
                                    },
                                    onFailure = {
                                        onError(HealthFitnessError.UNSUBSCRIBE_ERROR)
                                    })
                            }
                        }
                    }
                    else {
                        onError(HealthFitnessError.BACKGROUND_JOB_DOES_NOT_EXISTS_ERROR)
                    }
                }
                catch (e: Exception){
                    onError(HealthFitnessError.DELETE_BACKGROUND_JOB_GENERIC_ERROR)
                }
            }
        }
    }

    override fun listBackgroundJobs(onSuccess : (BackgroundJobsResponse) -> Unit,
                           onError: (HealthFitnessError) -> Unit) {

        runBlocking {
            launch(Dispatchers.IO) {
                try {
                    var jobsList = database.fetchBackgroundJobs()!!
                    onSuccess(BackgroundJobsResponse(buildListBackgroundJobsResult(jobsList)))
                }
                catch (e: Exception){
                    onError(HealthFitnessError.LIST_BACKGROUND_JOBS_GENERIC_ERROR)
                }
            }
        }
    }

    private fun buildListBackgroundJobsResult(jobsList: List<BackgroundJob>) : List<BackgroundJobsResponseBlock>{
        val responseJobList : MutableList<BackgroundJobsResponseBlock> = mutableListOf()
        for (job in jobsList){
            val notification = database.fetchNotification(job.notificationId!!)
            responseJobList.add(
                BackgroundJobsResponseBlock(
                    job.variable,
                    job.comparison,
                    job.value,
                    notification?.title,
                    notification?.body,
                    job.notificationFrequency,
                    job.notificationFrequencyGrouping,
                    job.isActive,
                    job.id
                )
            )
        }
        return responseJobList
    }

    override fun updateBackgroundJob(parameters: UpdateBackgroundJobParameters,
                            onSuccess: (String) -> Unit,
                            onError: (HealthFitnessError) -> Unit) {

        runBlocking {
            launch(Dispatchers.IO) {

                try {
                    val job = database.fetchBackgroundJob(parameters.id)
                    if(job != null) {
                        val notification = database.fetchNotification(job.notificationId!!)
                        if(parameters.value != null){
                            job.value = parameters.value
                        }
                        if(parameters.condition != null){
                            job.comparison = parameters.condition
                        }
                        if(parameters.isActive != null){
                            job.isActive = parameters.isActive
                        }
                        if(parameters.notificationFrequency != null){
                            job.notificationFrequency = parameters.notificationFrequency
                            job.nextNotificationTimestamp = 0
                        }
                        if(parameters.notificationFrequencyGrouping != null){
                            job.notificationFrequencyGrouping = parameters.notificationFrequencyGrouping
                            job.nextNotificationTimestamp = 0
                        }
                        if(notification != null){
                            if(parameters.notificationHeader != null){
                                notification.title = parameters.notificationHeader
                            }
                            if(parameters.notificationBody != null){
                                notification.body = parameters.notificationBody
                            }
                            database.updateNotification(notification)
                        }
                        database.updateBackgroundJob(job)
                        onSuccess("success")
                    }
                    else {
                        onError(HealthFitnessError.BACKGROUND_JOB_DOES_NOT_EXISTS_ERROR)
                    }
                }
                catch (e: Exception){
                    onError(HealthFitnessError.UPDATE_BACKGROUND_JOB_GENERIC_ERROR)
                }

            }
        }

    }

    companion object {
        const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 2
    }

}
