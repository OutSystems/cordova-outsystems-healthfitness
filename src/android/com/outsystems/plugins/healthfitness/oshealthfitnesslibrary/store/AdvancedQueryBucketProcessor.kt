package com.outsystems.plugins.healthfitnesslib.store

import com.google.android.gms.fitness.data.Bucket
import com.google.android.gms.fitness.data.DataPoint
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class AdvancedQueryBucketProcessor {

    companion object {

        fun processIntoBuckets(buckets : List<Bucket>): List<ProcessedBucket> {

            val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val processedBuckets: MutableList<ProcessedBucket> = mutableListOf()
            for (bucket in buckets) {

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
            }
            return processedBuckets
        }

        fun processIntoBuckets(
            startDate: Date,
            endDate: Date,
            timeUnit: EnumTimeUnit?,
            timeUnitLength: Int?,
            dataPoints: List<DataPoint>
        ): List<ProcessedBucket> {

            when (timeUnit) {
                EnumTimeUnit.WEEK -> {
                    return processIntoWeekBuckets(
                        startDate.time,
                        endDate.time,
                        timeUnitLength ?: 1,
                        dataPoints
                    )
                }
                EnumTimeUnit.MONTH -> {
                    return processIntoMonthBuckets(
                        startDate.time,
                        endDate.time,
                        timeUnitLength ?: 1,
                        dataPoints
                    )
                }
                EnumTimeUnit.YEAR -> {
                    return processIntoYearBuckets(
                        startDate.time,
                        endDate.time,
                        timeUnitLength ?: 1,
                        dataPoints
                    )
                }
                else -> {
                    return processIntoDayBuckets(
                        startDate.time,
                        endDate.time,
                        1,
                        dataPoints
                    )
                }
            }
        }

        private fun processIntoDayBuckets(
            queryStartDate: Long,
            queryEndDate: Long,
            timeUnitLength: Int,
            dataPoints: List<DataPoint>
        ): List<ProcessedBucket> {

            val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val processedBuckets: MutableMap<String, ProcessedBucket> = mutableMapOf()
            val bucketKeyQueue: ArrayDeque<String> = ArrayDeque()

            dataPoints.forEach { dataPoint ->
                val dayNumber = getDayNumber(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val yearNumber = getYear(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val dataPointKey = "$dayNumber$yearNumber"

                if (!processedBuckets.containsKey(dataPointKey)) {

                    val c = Calendar.getInstance()
                    c.set(Calendar.DAY_OF_YEAR, dayNumber)
                    c.set(Calendar.YEAR, yearNumber)
                    c.set(Calendar.DAY_OF_MONTH, 1)
                    c.set(Calendar.HOUR, 0)
                    c.set(Calendar.MINUTE, 0)
                    c.set(Calendar.SECOND, 0)

                    var startDate = c.timeInMillis
                    if (startDate < queryStartDate) {
                        startDate = queryStartDate
                    }

                    c.add(Calendar.MONTH, 1)
                    var endDate = c.timeInMillis
                    if (endDate > queryEndDate) {
                        endDate = queryEndDate
                    }

                    processedBuckets[dataPointKey] = ProcessedBucket(
                        startDate,
                        endDate,
                        mutableListOf(),
                        mutableListOf(),
                        format.format(startDate),
                        format.format(endDate)
                    )
                    bucketKeyQueue.push(dataPointKey)
                }

                processedBuckets[dataPointKey]!!.dataPoints.add(dataPoint)
            }
            mergeBucketsIntoGroups(timeUnitLength, bucketKeyQueue, processedBuckets)
            return processedBuckets.values.toList()

        }

        private fun processIntoWeekBuckets(
            queryStartDate: Long,
            queryEndDate: Long,
            timeUnitLength: Int,
            dataPoints: List<DataPoint>
        ): List<ProcessedBucket> {
            val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val processedBuckets: MutableMap<String, ProcessedBucket> = mutableMapOf()
            val weekKeyQueue: ArrayDeque<String> = ArrayDeque()
            dataPoints.forEach { dataPoint ->

                val weekNumber = getISOWeekNumber(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val yearNumber = getYear(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val dataPointKey = "$weekNumber$yearNumber"

                if (!processedBuckets.containsKey(dataPointKey)) {
                    val c = Calendar.getInstance()
                    c.set(Calendar.WEEK_OF_YEAR, weekNumber)

                    val firstDayOfWeek = c.firstDayOfWeek

                    c[Calendar.DAY_OF_WEEK] = firstDayOfWeek
                    var startDate = c.timeInMillis
                    if (startDate < queryStartDate) {
                        startDate = queryStartDate
                    }

                    c[Calendar.DAY_OF_WEEK] = firstDayOfWeek + 6
                    var endDate = c.timeInMillis
                    if (endDate > queryEndDate) {
                        endDate = queryEndDate
                    }

                    processedBuckets[dataPointKey] = ProcessedBucket(
                        startDate,
                        endDate,
                        mutableListOf(),
                        mutableListOf(),
                        format.format(startDate),
                        format.format(endDate)
                    )
                    weekKeyQueue.add(dataPointKey)
                }

                processedBuckets[dataPointKey]!!.dataPoints.add(dataPoint)
            }
            mergeBucketsIntoGroups(timeUnitLength, weekKeyQueue, processedBuckets)
            return processedBuckets.values.toList()
        }

        private fun processIntoMonthBuckets(
            queryStartDate: Long,
            queryEndDate: Long,
            timeUnitLength: Int,
            dataPoints: List<DataPoint>
        ): List<ProcessedBucket> {

            val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val processedBuckets: MutableMap<String, ProcessedBucket> = mutableMapOf()
            val bucketKeyQueue: ArrayDeque<String> = ArrayDeque()

            dataPoints.forEach { dataPoint ->
                val monthNumber = getMonth(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val yearNumber = getYear(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val dataPointKey = "$monthNumber$yearNumber"

                if (!processedBuckets.containsKey(dataPointKey)) {

                    val c = Calendar.getInstance()
                    c.set(Calendar.MONTH, monthNumber)
                    c.set(Calendar.YEAR, yearNumber)
                    c.set(Calendar.DAY_OF_MONTH, 1)
                    c.set(Calendar.HOUR, 0)
                    c.set(Calendar.MINUTE, 0)
                    c.set(Calendar.SECOND, 0)

                    var startDate = c.timeInMillis
                    if (startDate < queryStartDate) {
                        startDate = queryStartDate
                    }

                    c.add(Calendar.MONTH, 1)
                    var endDate = c.timeInMillis
                    if (endDate > queryEndDate) {
                        endDate = queryEndDate
                    }

                    processedBuckets[dataPointKey] = ProcessedBucket(
                        startDate,
                        endDate,
                        mutableListOf(),
                        mutableListOf(),
                        format.format(startDate),
                        format.format(endDate)
                    )
                    bucketKeyQueue.push(dataPointKey)
                }

                processedBuckets[dataPointKey]!!.dataPoints.add(dataPoint)
            }
            mergeBucketsIntoGroups(timeUnitLength, bucketKeyQueue, processedBuckets)
            return processedBuckets.values.toList()
        }

        private fun processIntoYearBuckets(
            queryStartDate: Long,
            queryEndDate: Long,
            timeUnitLength: Int,
            dataPoints: List<DataPoint>
        ): List<ProcessedBucket> {

            val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val processedBuckets: MutableMap<String, ProcessedBucket> = mutableMapOf()
            val bucketKeyQueue: ArrayDeque<String> = ArrayDeque()

            dataPoints.forEach { dataPoint ->

                val dataPointDate = Date(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val calendar = Calendar.getInstance()
                calendar.time = dataPointDate

                val yearNumber = calendar.get(Calendar.YEAR)
                val dataPointKey = "$yearNumber"

                if (!processedBuckets.containsKey(dataPointKey)) {

                    val c = Calendar.getInstance()
                    c.set(Calendar.YEAR, yearNumber)
                    c.set(Calendar.DAY_OF_YEAR, 1)

                    var startDate = c.timeInMillis
                    if (startDate < queryStartDate) {
                        startDate = queryStartDate
                    }

                    c.add(Calendar.YEAR, 1)
                    var endDate = c.timeInMillis
                    if (endDate > queryEndDate) {
                        endDate = queryEndDate
                    }

                    bucketKeyQueue.push(dataPointKey)
                    processedBuckets[dataPointKey] = ProcessedBucket(
                        startDate,
                        endDate,
                        mutableListOf(),
                        mutableListOf(),
                        format.format(startDate),
                        format.format(endDate)
                    )
                }
                processedBuckets[dataPointKey]!!.dataPoints.add(dataPoint)
            }

            mergeBucketsIntoGroups(timeUnitLength, bucketKeyQueue, processedBuckets)
            return processedBuckets.values.toList()
        }

        fun applyBucketOperation(
            variable : GoogleFitVariable,
            operationType : String,
            buckets : List<ProcessedBucket>
        ) : List<ProcessedBucket> {

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


        private fun mergeBucketsIntoGroups(
            length: Int,
            keys: ArrayDeque<String>,
            buckets: MutableMap<String, ProcessedBucket>
        ) {
            while (!keys.isEmpty()) {
                val keyAnchor = keys.pop()

                var limit = keys.size + 1
                if (length < limit) {
                    limit = length
                }

                for (i in 1 until limit) {

                    val keyToMergeWithAnchor = keys.pop()
                    val valuesToMerge = buckets[keyToMergeWithAnchor]!!

                    buckets[keyAnchor]!!.dataPoints.addAll(valuesToMerge.dataPoints)
                    buckets[keyAnchor]!!.endDate = valuesToMerge.endDate

                    buckets.remove(keyToMergeWithAnchor)
                }
            }
        }
        private fun getDayNumber(milliseconds: Long): Int {
            val isoCalendar = Calendar.getInstance()
            isoCalendar.time = Date(milliseconds)
            return isoCalendar.get(Calendar.DAY_OF_YEAR)
        }
        private fun getISOWeekNumber(milliseconds: Long): Int {
            val isoCalendar = Calendar.getInstance()
            isoCalendar.time = Date(milliseconds)
            isoCalendar.minimalDaysInFirstWeek = 4;
            isoCalendar.firstDayOfWeek = Calendar.MONDAY;
            return isoCalendar.get(Calendar.WEEK_OF_YEAR)
        }
        private fun getMonth(milliseconds: Long): Int {
            val isoCalendar = Calendar.getInstance()
            isoCalendar.time = Date(milliseconds)
            return isoCalendar.get(Calendar.MONTH)
        }
        private fun getYear(milliseconds: Long): Int {
            val isoCalendar = Calendar.getInstance()
            isoCalendar.time = Date(milliseconds)
            return isoCalendar.get(Calendar.YEAR)
        }

    }
}

