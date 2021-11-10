package com.outsystems.plugins.healthfitnesslib.store

import com.google.android.gms.fitness.data.Bucket
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private val timeUnitsForMinMaxAverage: Map<String, EnumTimeUnit> by lazy {
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
    private var bucketProcessor : BucketProcessor

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
        bucketProcessor = BucketProcessor(startDate.time, endDate.time)
    }

    fun setOperationType(operation : String?) {
        operation?.let {
            operationType = it

            if(operationType == EnumOperationType.RAW.value){
                if(dataSource != null) {
                    dataRequestBuilder.read(dataSource!!)
                }
                else {
                    dataRequestBuilder.read(variable.dataType)
                }
            }
            else {
                if(dataSource != null) {
                    dataRequestBuilder.aggregate(dataSource!!)
                }
                else {
                    dataRequestBuilder.aggregate(variable.dataType)
                }
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
            if(timeUnit!!.value.first == EnumTimeUnit.WEEK.value.first ||
                timeUnit!!.value.first == EnumTimeUnit.MONTH.value.first ||
                timeUnit!!.value.first == EnumTimeUnit.YEAR.value.first) {
                dataRequestBuilder.bucketByTime(1, timeUnit!!.value.second)
            }
            else {
                dataRequestBuilder.bucketByTime(grouping, timeUnit!!.value.second)
            }

            if(timeUnit!!.value.first == EnumTimeUnit.WEEK.value.first) {
                bucketProcessor = WeekBucketProcessor(grouping, startDate.time, endDate.time)
            }
            else if(timeUnit!!.value.first == EnumTimeUnit.MONTH.value.first) {
                bucketProcessor = MonthBucketProcessor(grouping, startDate.time, endDate.time)
            }
            else if(timeUnit!!.value.first == EnumTimeUnit.YEAR.value.first) {
                bucketProcessor = YearBucketProcessor(grouping, startDate.time, endDate.time)
            }
        }
    }

    fun getDataReadRequest() : DataReadRequest {
        if(timeUnit != null && timeUnitLength == null) {
            dataRequestBuilder.bucketByTime(1, timeUnit!!.value.second)
        }
        return dataRequestBuilder.build()
    }

    fun processBuckets(buckets : List<Bucket>) : List<ProcessedBucket> {
        val joinedBuckets = bucketProcessor.process(buckets)
        return processBucketOperation(joinedBuckets)
    }

    fun isSingleResult() : Boolean {
        return limit != null && limit == 1
    }

    private fun processBucketOperation(buckets : List<ProcessedBucket>) : List<ProcessedBucket> {

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

    private open class BucketProcessor (val queryStartDate : Long, val queryEndDate : Long) {
        open fun process(buckets : List<Bucket>) : List<ProcessedBucket> {
            val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val processedBuckets : MutableList<ProcessedBucket> = mutableListOf()

            for(bucket in buckets) {

                val dataPointsPerBucket = bucket.dataSets.flatMap { it.dataPoints }
                //if(dataPointsPerBucket.isEmpty()){ continue }

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
    }

    private class WeekBucketProcessor(val timeUnitLength : Int, queryStartDate : Long, queryEndDate : Long) :
        BucketProcessor(queryStartDate, queryEndDate) {

        override fun process(buckets: List<Bucket>): List<ProcessedBucket> {

            val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val processedBuckets : MutableMap<String, ProcessedBucket> = mutableMapOf()
            val weekKeyQueue : ArrayDeque<String> = ArrayDeque()

            //Merge buckets into weeks
            for(bucket in buckets) {

                val dataPointsPerBucket = bucket.dataSets.flatMap { it.dataPoints }

                if(dataPointsPerBucket.isEmpty()){ continue }

                dataPointsPerBucket.forEach { dataPoint ->

                    val dataPointDate = Date(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                    val isoCalendar = Calendar.getInstance()
                    isoCalendar.time = dataPointDate
                    isoCalendar.minimalDaysInFirstWeek = 4;
                    isoCalendar.firstDayOfWeek = Calendar.MONDAY;

                    val weekNumber = isoCalendar.get(Calendar.WEEK_OF_YEAR)
                    val yearNumber = isoCalendar.get(Calendar.YEAR)
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

                var limit = weekKeyQueue.size + 1
                if(timeUnitLength < limit){ limit = timeUnitLength }

                for(i in 1 until limit) {

                    val keyToMergeWithAnchor = weekKeyQueue.pop()
                    val valuesToMerge = processedBuckets[keyToMergeWithAnchor]!!

                    processedBuckets[keyAnchor]!!.dataPoints.addAll(valuesToMerge.dataPoints)
                    processedBuckets[keyAnchor]!!.endDate = valuesToMerge.endDate

                    processedBuckets.remove(keyToMergeWithAnchor)
                }
            }

            return processedBuckets.values.toList()

        }
    }

    private class MonthBucketProcessor(val timeUnitLength : Int, queryStartDate : Long, queryEndDate : Long) :
        BucketProcessor(queryStartDate, queryEndDate) {

        override fun process(buckets: List<Bucket>): List<ProcessedBucket> {
            val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val processedBuckets : MutableMap<String, ProcessedBucket> = mutableMapOf()
            val bucketKeyQueue : ArrayDeque<String> = ArrayDeque()

            //Merge buckets into Months
            for(bucket in buckets) {

                val dataPointsPerBucket = bucket.dataSets.flatMap { it.dataPoints }
                if(dataPointsPerBucket.isEmpty()){ continue }

                dataPointsPerBucket.forEach { dataPoint ->

                    val dataPointDate = Date(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                    val calendar = Calendar.getInstance()
                    calendar.time = dataPointDate

                    val monthNumber = calendar.get(Calendar.MONTH)
                    val yearNumber = calendar.get(Calendar.YEAR)
                    val dataPointKey = "$monthNumber$yearNumber"

                    if(!processedBuckets.containsKey(dataPointKey)) {

                        val c = Calendar.getInstance()
                        c.set(Calendar.MONTH, monthNumber)
                        c.set(Calendar.YEAR, yearNumber)
                        c.set(Calendar.DAY_OF_MONTH, 1)
                        c.set(Calendar.HOUR, 0)
                        c.set(Calendar.MINUTE, 0)
                        c.set(Calendar.SECOND, 0)

                        var startDate = c.timeInMillis
                        if(startDate < queryStartDate) { startDate = queryStartDate }

                        c.add(Calendar.MONTH, 1)
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

                var limit = bucketKeyQueue.size + 1
                if(timeUnitLength < limit){ limit = timeUnitLength }

                for(i in 1 until limit) {

                    val keyToMergeWithAnchor = bucketKeyQueue.pop()
                    val valuesToMerge = processedBuckets[keyToMergeWithAnchor]!!

                    processedBuckets[keyAnchor]!!.dataPoints.addAll(valuesToMerge.dataPoints)
                    processedBuckets[keyAnchor]!!.endDate = valuesToMerge.endDate

                    processedBuckets.remove(keyToMergeWithAnchor)
                }
            }

            return processedBuckets.values.toList()
        }
    }

    private class YearBucketProcessor(val timeUnitLength : Int, queryStartDate : Long, queryEndDate : Long) :
        BucketProcessor(queryStartDate, queryEndDate) {

        override fun process(buckets: List<Bucket>): List<ProcessedBucket> {
            val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val processedBuckets : MutableMap<String, ProcessedBucket> = mutableMapOf()
            val bucketKeyQueue : ArrayDeque<String> = ArrayDeque()

            //Merge buckets into Years
            for(bucket in buckets) {

                val dataPointsPerBucket = bucket.dataSets.flatMap { it.dataPoints }
                if(dataPointsPerBucket.isEmpty()){ continue }

                dataPointsPerBucket.forEach { dataPoint ->

                    val dataPointDate = Date(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                    val calendar = Calendar.getInstance()
                    calendar.time = dataPointDate

                    val yearNumber = calendar.get(Calendar.YEAR)
                    val dataPointKey = "$yearNumber"

                    if(!processedBuckets.containsKey(dataPointKey)) {

                        val c = Calendar.getInstance()
                        c.set(Calendar.YEAR, yearNumber)
                        c.set(Calendar.DAY_OF_YEAR, 1)

                        var startDate = c.timeInMillis
                        if(startDate < queryStartDate) { startDate = queryStartDate }

                        c.add(Calendar.YEAR, 1)
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

                var limit = bucketKeyQueue.size + 1
                if(timeUnitLength < limit){ limit = timeUnitLength }

                for(i in 1 until limit) {

                    val keyToMergeWithAnchor = bucketKeyQueue.pop()
                    val valuesToMerge = processedBuckets[keyToMergeWithAnchor]!!

                    processedBuckets[keyAnchor]!!.dataPoints.addAll(valuesToMerge.dataPoints)
                    processedBuckets[keyAnchor]!!.endDate = valuesToMerge.endDate

                    processedBuckets.remove(keyToMergeWithAnchor)
                }
            }

            return processedBuckets.values.toList()
        }
    }
}