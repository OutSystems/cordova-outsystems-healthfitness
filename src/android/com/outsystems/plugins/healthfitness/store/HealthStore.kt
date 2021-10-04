package com.outsystems.plugins.healthfitness.store

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.gson.Gson
import com.outsystems.plugins.healthfitness.AndroidPlatformInterface
import com.outsystems.plugins.healthfitness.HealthFitnessError
import com.outsystems.plugins.healthfitness.OSHealthFitness
import java.text.SimpleDateFormat
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
            "PUSH_COUNT" to GoogleFitVariable(DataType.TYPE_ACTIVITY_SEGMENT, listOf(
                //TODO: possible different from iOS
            ),
            listOf(

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
            ))
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

    fun initAndRequestPermissions(
        customPermissions: String,
        allVariables: String,
        fitnessVariables: String,
        healthVariables: String,
        profileVariables: String,
        summaryVariables: String
    ) {

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
        account = GoogleSignIn.getAccountForExtension(context, fitnessOptions!!)
    }

    private fun createPermissionsForVariableGroup(
        permission: String,
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

    private fun createPermissionsForVariable(
        variable: GoogleFitVariable,
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

    private fun createFitnessOptions(permissionList: List<Pair<DataType, Int>>) : FitnessOptions{
        val fitnessBuild = FitnessOptions.builder()
        permissionList.forEach {
            fitnessBuild.addDataType(it.first, it.second)
        }
        return fitnessBuild.build()
    }

    fun requestGoogleFitPermissions() {
        if(areGoogleFitPermissionsGranted(account, fitnessOptions)){
            platformInterface.sendPluginResult("success")
        }
        else{
            fitnessOptions?.let {
                GoogleSignIn.requestPermissions(
                    platformInterface.getActivity(),
                    OSHealthFitness.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    account,
                    it
                )
            }
        }
    }

    fun areGoogleFitPermissionsGranted(): Boolean {
        return areGoogleFitPermissionsGranted(account, fitnessOptions);
    }

    private fun areGoogleFitPermissionsGranted(account : GoogleSignInAccount?, options: FitnessOptions?): Boolean {
        account.let {
            options.let {
                return GoogleSignIn.hasPermissions(account, options)
            }
        }
    }

    fun updateData(variableName: String, value: Float) {

        //right now we are only writing data which are float values
        val variable = getVariableByName(variableName)
        if(variable == null) {
            platformInterface.sendPluginResult(
                null,
                Pair(HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.code, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.message))
            return
        }

        val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
        val permissions = createPermissionsForVariable(variable, EnumAccessType.WRITE.value)
        val options = createFitnessOptions(permissions)
        if(!areGoogleFitPermissionsGranted(lastAccount, options)) {
            platformInterface.sendPluginResult(
                null,
                Pair(HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.code, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.message))
            return
        }

        //profile variables only have 1 field each, so it is safe to get the first entry of the fields list
        val fieldType = profileVariablesMap[variableName]?.fields?.get(0)

        //insert the data
        val dataSourceWrite = DataSource.Builder()
                .setAppPackageName(context)
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
        } else{
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
        }catch (e : IllegalArgumentException){
            Log.w("Write to GoogleFit:", "Field out of range", e)
            platformInterface.sendPluginResult(null, Pair(HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR.code, HealthFitnessError.WRITE_VALUE_OUT_OF_RANGE_ERROR.message))
        }

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

    fun getLastRecord(variable: String) {

        val endDate: Long = Date().time
        val month = 2592000000
        val startDate: Long = endDate - month

        val advancedQueryParameters = AdvancedQueryParameters(
            variable,
            Date(startDate),
            Date(endDate),
            limit = 1
        )
        advancedQuery(advancedQueryParameters)
    }

    fun advancedQuery(parameters : AdvancedQueryParameters) {

        val variable = getVariableByName(parameters.variable)
        val endDate = parameters.endDate
        val startDate = parameters.startDate

        if(variable == null) {
            platformInterface.sendPluginResult(
                null,
                Pair(HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.code, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.message))
            return
        }

        if(!variable.allowedOperations.contains(parameters.operationType)) {
            platformInterface.sendPluginResult(
                null,
                Pair(HealthFitnessError.OPERATION_NOT_ALLOWED.code, HealthFitnessError.OPERATION_NOT_ALLOWED.message))
            return
        }

        val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
        val permissions = createPermissionsForVariable(variable, EnumAccessType.READ.value)
        val options = createFitnessOptions(permissions)
        if(!areGoogleFitPermissionsGranted(lastAccount, options)) {
            platformInterface.sendPluginResult(
                null,
                Pair(HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.code, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.message))
            return
        }

        val queryInformation = AdvancedQuery(variable, startDate, endDate)
        queryInformation.setOperationType(parameters.operationType)
        queryInformation.setTimeUnit(parameters.timeUnit)
        queryInformation.setTimeUnitGrouping(parameters.timeUnitLength)
        queryInformation.setLimit(parameters.limit)

        val fitnessRequest = queryInformation.getDataReadRequest()
        Fitness.getHistoryClient(context, lastAccount)
            .readData(fitnessRequest)
            .addOnSuccessListener { dataReadResponse: DataReadResponse ->

                val queryResponse: AdvancedQueryResponse

                if(queryInformation.isSingleResult()) {

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
                        startDate.time / 1000,
                        endDate.time / 1000,
                        values
                    )
                    queryResponse = AdvancedQueryResponse(listOf(responseBlock))
                }
                else {
                    val resultBuckets = queryInformation.processBuckets(dataReadResponse.buckets)
                    queryResponse = buildAdvancedQueryResult(resultBuckets)
                }

                if(parameters.variable == "HEIGHT"){
                    queryResponse.results.forEach{ bucket ->
                        for (i in bucket.values.indices){
                            bucket.values[i] = bucket.values[i] * 100
                        }
                    }
                }

                val pluginResponseJson = gson.toJson(queryResponse)
                Log.d("STORE", "Response $pluginResponseJson")
                platformInterface.sendPluginResult(pluginResponseJson)

            }
            .addOnFailureListener { dataReadResponse: Exception ->
                platformInterface.sendPluginResult(
                    null,
                    Pair(HealthFitnessError.READ_DATA_ERROR.code, HealthFitnessError.READ_DATA_ERROR.message))
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

}
