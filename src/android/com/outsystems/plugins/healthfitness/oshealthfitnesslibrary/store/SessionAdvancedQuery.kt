package com.outsystems.plugins.healthfitnesslib.store

import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.SessionReadRequest
import java.util.*
import java.util.concurrent.TimeUnit

class SessionAdvancedQuery(
    private val variable : GoogleFitVariable,
    private val startDate : Date,
    private val endDate : Date)
{
    private var dataSource : DataSource? = null
    private var readRequest : SessionReadRequest.Builder = SessionReadRequest.Builder()
    private var operationType : String = EnumOperationType.RAW.value
    private var timeUnit : EnumTimeUnit? = null
    private var timeUnitLength : Int? = null
    private var limit : Int? = null

    init {
        if(variable.dataType == DataType.TYPE_STEP_COUNT_DELTA) {
            //This is the special case for step count
            dataSource = DataSource.Builder()
                .setAppPackageName("com.google.android.gms")
                .setDataType(variable.dataType)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .build()
        }
        readRequest.readSessionsFromAllApps()
        readRequest.enableServerQueries()
        readRequest.includeActivitySessions()
        readRequest.setTimeInterval(startDate.time, endDate.time, TimeUnit.MILLISECONDS)
    }

    fun setOperationType(operation : String?) {
        operation?.let {
            operationType = it

            if(dataSource != null) {
                readRequest.read(dataSource!!)
            }
            else {
                readRequest.read(variable.dataType)
            }
        }
    }
    fun setTimeUnit(unit : String?) {
        unit?.let {
            timeUnit = if(operationType == EnumOperationType.SUM.value || operationType == EnumOperationType.RAW.value) {
                timeUnits[unit] ?: EnumTimeUnit.DAY
            } else {
                timeUnitsForMinMaxAverage[unit] ?: EnumTimeUnit.DAY
            }
        }
    }
    fun setTimeUnitGrouping(grouping : Int?) {
        if(grouping != null && timeUnit != null) {
            timeUnitLength = grouping
            if(timeUnit!!.value.first == EnumTimeUnit.WEEK.value.first ||
                timeUnit!!.value.first == EnumTimeUnit.MONTH.value.first ||
                timeUnit!!.value.first == EnumTimeUnit.YEAR.value.first) {
                //dataRequestBuilder.bucketByTime(1, timeUnit!!.value.second)
            }
            else {
                //dataRequestBuilder.bucketByTime(grouping, timeUnit!!.value.second)
            }
        }
    }

    fun getDataReadRequest() : SessionReadRequest {
        return readRequest.build()
    }

    fun isSingleResult() : Boolean {
        return limit != null && limit == 1
    }

    fun processIntoBuckets(dataPoints : List<DataPoint>) : List<ProcessedBucket>{
        val buckets = AdvancedQueryBucketProcessor.processIntoBuckets(
            startDate,
            endDate,
            timeUnit,
            timeUnitLength,
            dataPoints)
        return applyBucketOperation(buckets)
    }

    private fun applyBucketOperation(buckets : List<ProcessedBucket>) : List<ProcessedBucket>{
        return AdvancedQueryBucketProcessor.applyBucketOperation(
            variable,
            operationType,
            buckets)
    }

}