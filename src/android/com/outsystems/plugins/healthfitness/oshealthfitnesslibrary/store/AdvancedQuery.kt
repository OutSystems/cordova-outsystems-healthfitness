package com.outsystems.plugins.healthfitnesslib.store

import android.util.Log
import com.google.android.gms.fitness.data.Bucket
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import java.util.*
import java.util.concurrent.TimeUnit

val timeUnitsForMinMaxAverage: Map<String, EnumTimeUnit> by lazy {
    mapOf(
        "MILLISECONDS" to EnumTimeUnit.MILLISECOND,
        "SECONDS" to EnumTimeUnit.MILLISECOND,
        "MINUTE" to EnumTimeUnit.SECOND,
        "HOUR" to EnumTimeUnit.MINUTE,
        "DAY" to EnumTimeUnit.HOUR,
        "WEEK" to EnumTimeUnit.DAY,
        "MONTH" to EnumTimeUnit.WEEK,
        "YEAR" to EnumTimeUnit.MONTH
    )
}
val timeUnits: Map<String, EnumTimeUnit> by lazy {
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

data class ProcessedBucket(
    val startDate : Long,
    var endDate : Long,
    var dataPoints : MutableList<DataPoint> = mutableListOf(),
    var processedDataPoints : MutableList<Float> = mutableListOf(),
    var DEBUG_startDate : String = "",
    var DEBUG_endDate : String = ""
)

class AdvancedQuery(
    private val variable : GoogleFitVariable,
    private val startDate : Date,
    private val endDate : Date)
{
    private var dataSource : DataSource? = null
    private var dataRequestBuilder : DataReadRequest.Builder = DataReadRequest.Builder()
    private var operationType : String = EnumOperationType.RAW.value
    private var timeUnit : EnumTimeUnit? = null
    private var timeUnitLength : Int? = null
    private var limit : Int? = null
    private var bucketBySession : Boolean = false

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
        dataRequestBuilder.setTimeRange(startDate.time, endDate.time, TimeUnit.MILLISECONDS)
        bucketBySession = variable.filterBySession.isNotEmpty()
    }

    fun setOperationType(operation : String?) {
        operation?.let {
            operationType = it
            if(dataSource != null) {
                dataRequestBuilder.read(dataSource!!)
            }
            else {
                dataRequestBuilder.read(variable.dataType)
            }
        }
    }
    fun setLimit(count : Int?) {
        count?.let {
            limit = it
            dataRequestBuilder.setLimit(it)
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
            if(bucketBySession){
                dataRequestBuilder.bucketBySession(1, TimeUnit.SECONDS)
            }
            else {
                if(timeUnit!!.value.first == EnumTimeUnit.WEEK.value.first ||
                    timeUnit!!.value.first == EnumTimeUnit.MONTH.value.first ||
                    timeUnit!!.value.first == EnumTimeUnit.YEAR.value.first) {
                    dataRequestBuilder.bucketByTime(1, timeUnit!!.value.second)
                }
                else {
                    dataRequestBuilder.bucketByTime(grouping, timeUnit!!.value.second)
                }
            }
        }
    }

    fun getDataReadRequest() : DataReadRequest {
        if(timeUnit != null && timeUnitLength == null) {
            dataRequestBuilder.bucketByTime(1, timeUnit!!.value.second)
        }
        return dataRequestBuilder.build()
    }

    fun isSingleResult() : Boolean {
        return limit != null && limit == 1
    }

    fun processBuckets(buckets : List<Bucket>) : List<ProcessedBucket>{

        var filteredBuckets = buckets
        if(bucketBySession){
            filteredBuckets = buckets.filter {
                it.session.activity in variable.filterBySession
            }
        }

        val buckets = AdvancedQueryBucketProcessor.processBuckets(
            startDate,
            endDate,
            timeUnit,
            timeUnitLength,
            filteredBuckets)
        return applyBucketOperation(buckets)
    }

    private fun applyBucketOperation(buckets : List<ProcessedBucket>) : List<ProcessedBucket>{
        return AdvancedQueryBucketProcessor.applyBucketOperation(
            variable,
            operationType,
            buckets)
    }
}