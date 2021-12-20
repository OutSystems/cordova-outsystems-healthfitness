package com.outsystems.plugins.healthfitness.store

import android.util.Log
import com.google.android.gms.fitness.data.Bucket
import com.google.android.gms.fitness.data.DataPoint
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class AdvancedQueryBucketProcessor {

    companion object {

        fun processBuckets(
            startDate: Date,
            endDate: Date,
            timeUnit: EnumTimeUnit?,
            timeUnitLength: Int?,
            buckets: List<Bucket>
        ): List<ProcessedBucket> {

            val dataPoints = buckets.flatMap { it.dataSets }.flatMap { it.dataPoints }
            when (timeUnit) {
                EnumTimeUnit.SECOND -> {
                    return processIntoSecondBuckets(
                        startDate.time,
                        endDate.time,
                        timeUnitLength ?: 1,
                        dataPoints
                    )
                }
                EnumTimeUnit.MINUTE -> {
                    return processIntoMinuteBuckets(
                        startDate.time,
                        endDate.time,
                        timeUnitLength ?: 1,
                        dataPoints
                    )
                }
                EnumTimeUnit.HOUR -> {
                    return processIntoHourBuckets(
                        startDate.time,
                        endDate.time,
                        timeUnitLength ?: 1,
                        dataPoints
                    )
                }
                EnumTimeUnit.DAY -> {
                    return processIntoDayBuckets(
                        startDate.time,
                        endDate.time,
                        timeUnitLength ?: 1,
                        dataPoints
                    )
                }
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
                    return processBuckets(buckets)
                }
            }
        }

        private fun processBuckets(buckets : List<Bucket>): List<ProcessedBucket> {
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


        private fun processIntoSecondBuckets(
            queryStartDate: Long,
            queryEndDate: Long,
            timeUnitLength: Int,
            dataPoints: List<DataPoint>
        ): List<ProcessedBucket> {

            val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val processedBuckets: MutableMap<String, ProcessedBucket> = mutableMapOf()
            val bucketKeyQueue: ArrayDeque<String> = ArrayDeque()

            val currentCalendar = Calendar.getInstance()
            currentCalendar.time = Date(queryStartDate)
            while(currentCalendar.timeInMillis <= queryEndDate) {
                val secondNumber = getSecondNumber(currentCalendar.timeInMillis)
                val minuteNumber = getMinuteNumber(currentCalendar.timeInMillis)
                val hourNumber = getHourNumber(currentCalendar.timeInMillis)
                val dayNumber = getDayNumber(currentCalendar.timeInMillis)
                val monthNumber = getMonth(currentCalendar.timeInMillis)
                val yearNumber = getYear(currentCalendar.timeInMillis)
                val dataPointKey = "$secondNumber$minuteNumber$hourNumber$dayNumber$monthNumber$yearNumber"

                val c = Calendar.getInstance()
                c.set(Calendar.MONTH, monthNumber)
                c.set(Calendar.YEAR, yearNumber)
                c.set(Calendar.DAY_OF_MONTH, dayNumber)
                c.set(Calendar.HOUR_OF_DAY, hourNumber)
                c.set(Calendar.MINUTE, minuteNumber)
                c.set(Calendar.SECOND, secondNumber)
                c.clear(Calendar.MILLISECOND)

                var startDate = c.timeInMillis
                c.add(Calendar.SECOND, 1)
                var endDate = c.timeInMillis

                /*
                if (startDate < queryStartDate) {
                    startDate = queryStartDate
                }
                if (endDate > queryEndDate) {
                    endDate = queryEndDate
                }
                */

                processedBuckets[dataPointKey] = ProcessedBucket(
                    startDate,
                    endDate,
                    mutableListOf(),
                    mutableListOf(),
                    format.format(startDate),
                    format.format(endDate)
                )
                bucketKeyQueue.push(dataPointKey)
                currentCalendar.add(Calendar.SECOND, 1)
            }

            dataPoints.forEach { dataPoint ->
                val secondNumber = getSecondNumber(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val minuteNumber = getMinuteNumber(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val hourNumber = getHourNumber(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val dayNumber = getDayNumber(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val monthNumber = getMonth(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val yearNumber = getYear(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val dataPointKey = "$secondNumber$minuteNumber$hourNumber$dayNumber$monthNumber$yearNumber"
                processedBuckets[dataPointKey]!!.dataPoints.add(dataPoint)
            }

            mergeBucketsIntoGroups(timeUnitLength, bucketKeyQueue, processedBuckets)
            return processedBuckets.values.toList()
        }

        private fun processIntoMinuteBuckets(
            queryStartDate: Long,
            queryEndDate: Long,
            timeUnitLength: Int,
            dataPoints: List<DataPoint>
        ): List<ProcessedBucket> {

            val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val processedBuckets: MutableMap<String, ProcessedBucket> = mutableMapOf()
            val bucketKeyQueue: ArrayDeque<String> = ArrayDeque()

            val currentCalendar = Calendar.getInstance()
            currentCalendar.time = Date(queryStartDate)
            while(currentCalendar.timeInMillis <= queryEndDate) {

                val minuteNumber = getMinuteNumber(currentCalendar.timeInMillis)
                val hourNumber = getHourNumber(currentCalendar.timeInMillis)
                val dayNumber = getDayNumber(currentCalendar.timeInMillis)
                val monthNumber = getMonth(currentCalendar.timeInMillis)
                val yearNumber = getYear(currentCalendar.timeInMillis)
                val dataPointKey = "$minuteNumber$hourNumber$dayNumber$monthNumber$yearNumber"

                val c = Calendar.getInstance()
                c.set(Calendar.MONTH, monthNumber)
                c.set(Calendar.YEAR, yearNumber)
                c.set(Calendar.DAY_OF_MONTH, dayNumber)
                c.set(Calendar.HOUR_OF_DAY, hourNumber)
                c.set(Calendar.MINUTE, minuteNumber)
                c.set(Calendar.SECOND, 0)

                var startDate = c.timeInMillis
                c.add(Calendar.MINUTE, 1)
                var endDate = c.timeInMillis

                /*
                if (startDate < queryStartDate) {
                    startDate = queryStartDate
                }
                if (endDate > queryEndDate) {
                    endDate = queryEndDate
                }
                */

                processedBuckets[dataPointKey] = ProcessedBucket(
                    startDate,
                    endDate,
                    mutableListOf(),
                    mutableListOf(),
                    format.format(startDate),
                    format.format(endDate)
                )
                bucketKeyQueue.push(dataPointKey)
                currentCalendar.add(Calendar.MINUTE, 1)
            }

            dataPoints.forEach { dataPoint ->
                val minuteNumber = getMinuteNumber(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val hourNumber = getHourNumber(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val dayNumber = getDayNumber(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val monthNumber = getMonth(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val yearNumber = getYear(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val dataPointKey = "$minuteNumber$hourNumber$dayNumber$monthNumber$yearNumber"
                processedBuckets[dataPointKey]!!.dataPoints.add(dataPoint)
            }

            mergeBucketsIntoGroups(timeUnitLength, bucketKeyQueue, processedBuckets)
            return processedBuckets.values.toList()
        }

        private fun processIntoHourBuckets(
            queryStartDate: Long,
            queryEndDate: Long,
            timeUnitLength: Int,
            dataPoints: List<DataPoint>
        ): List<ProcessedBucket> {

            val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val processedBuckets: MutableMap<String, ProcessedBucket> = mutableMapOf()
            val bucketKeyQueue: ArrayDeque<String> = ArrayDeque()

            val currentCalendar = Calendar.getInstance()
            currentCalendar.time = Date(queryStartDate)
            while(currentCalendar.timeInMillis <= queryEndDate) {

                val hourNumber = getHourNumber(currentCalendar.timeInMillis)
                val dayNumber = getDayNumber(currentCalendar.timeInMillis)
                val monthNumber = getMonth(currentCalendar.timeInMillis)
                val yearNumber = getYear(currentCalendar.timeInMillis)
                val dataPointKey = "$hourNumber$dayNumber$monthNumber$yearNumber"

                val c = Calendar.getInstance()
                c.set(Calendar.YEAR, yearNumber)
                c.set(Calendar.MONTH, monthNumber)
                c.set(Calendar.DAY_OF_MONTH, dayNumber)
                c.set(Calendar.HOUR_OF_DAY, hourNumber)
                c.set(Calendar.MINUTE, 0)
                c.set(Calendar.SECOND, 0)

                var startDate = c.timeInMillis
                c.add(Calendar.HOUR, 1)
                var endDate = c.timeInMillis

                /*
                if (startDate < queryStartDate) {
                    startDate = queryStartDate
                }
                if (endDate > queryEndDate) {
                    endDate = queryEndDate
                }
                */

                processedBuckets[dataPointKey] = ProcessedBucket(
                    startDate,
                    endDate,
                    mutableListOf(),
                    mutableListOf(),
                    format.format(startDate),
                    format.format(endDate)
                )
                bucketKeyQueue.push(dataPointKey)
                currentCalendar.add(Calendar.HOUR, 1)
            }

            dataPoints.forEach { dataPoint ->
                val hourNumber = getHourNumber(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val dayNumber = getDayNumber(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val monthNumber = getMonth(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val yearNumber = getYear(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val dataPointKey = "$hourNumber$dayNumber$monthNumber$yearNumber"
                processedBuckets[dataPointKey]!!.dataPoints.add(dataPoint)
            }

            mergeBucketsIntoGroups(timeUnitLength, bucketKeyQueue, processedBuckets)
            return processedBuckets.values.toList()
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

            val currentCalendar = Calendar.getInstance()
            currentCalendar.time = Date(queryStartDate)
            while(currentCalendar.timeInMillis <= queryEndDate) {

                val dayNumber = getDayNumber(currentCalendar.timeInMillis)
                val monthNumber = getMonth(currentCalendar.timeInMillis)
                val yearNumber = getYear(currentCalendar.timeInMillis)
                val dataPointKey = "$dayNumber$monthNumber$yearNumber"

                val c = Calendar.getInstance()
                c.set(Calendar.YEAR, yearNumber)
                c.set(Calendar.MONTH, monthNumber)
                c.set(Calendar.DAY_OF_MONTH, dayNumber)
                c.set(Calendar.HOUR_OF_DAY, 0)
                c.set(Calendar.MINUTE, 0)
                c.set(Calendar.SECOND, 0)

                var startDate = c.timeInMillis
                c.add(Calendar.DATE, 1)
                var endDate = c.timeInMillis

                /*
                if (startDate < queryStartDate) {
                    startDate = queryStartDate
                }
                if (endDate > queryEndDate) {
                    endDate = queryEndDate
                }
                */

                processedBuckets[dataPointKey] = ProcessedBucket(
                    startDate,
                    endDate,
                    mutableListOf(),
                    mutableListOf(),
                    format.format(startDate),
                    format.format(endDate)
                )
                bucketKeyQueue.push(dataPointKey)
                currentCalendar.add(Calendar.DATE, 1)
            }

            dataPoints.forEach { dataPoint ->
                val dayNumber = getDayNumber(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val monthNumber = getMonth(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val yearNumber = getYear(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val dataPointKey = "$dayNumber$monthNumber$yearNumber"
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

            var currentCalendar = Calendar.getInstance()
            currentCalendar.time = Date(queryStartDate)
            currentCalendar.set(Calendar.HOUR_OF_DAY, 0)
            currentCalendar.clear(Calendar.MINUTE)
            currentCalendar.clear(Calendar.SECOND)
            currentCalendar.clear(Calendar.MILLISECOND)
            currentCalendar.minimalDaysInFirstWeek = 4
            currentCalendar.firstDayOfWeek = Calendar.MONDAY
            currentCalendar.set(Calendar.DAY_OF_WEEK, currentCalendar.firstDayOfWeek)

            while(currentCalendar.timeInMillis <= queryEndDate) {

                val weekNumber = getISOWeekNumber(currentCalendar.timeInMillis)
                val yearNumber = getYear(currentCalendar.timeInMillis)
                val dataPointKey = "$weekNumber$yearNumber"

                var startDate = currentCalendar.timeInMillis
                currentCalendar.add(Calendar.WEEK_OF_YEAR, 1)
                var endDate = currentCalendar.timeInMillis

                /*
                if (startDate < queryStartDate) {
                    startDate = queryStartDate
                }
                if (endDate > queryEndDate) {
                    endDate = queryEndDate
                }
                */

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

            dataPoints.forEach { dataPoint ->
                val weekNumber = getISOWeekNumber(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val yearNumber = getYear(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val dataPointKey = "$weekNumber$yearNumber"
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

            val currentCalendar = Calendar.getInstance()
            currentCalendar.time = Date(queryStartDate)
            while(currentCalendar.timeInMillis <= queryEndDate) {
                val monthNumber = getMonth(currentCalendar.timeInMillis)
                val yearNumber = getYear(currentCalendar.timeInMillis)
                val dataPointKey = "$monthNumber$yearNumber"

                val c = Calendar.getInstance()
                c.set(Calendar.MONTH, monthNumber)
                c.set(Calendar.YEAR, yearNumber)
                c.set(Calendar.DAY_OF_MONTH, 1)
                c.set(Calendar.HOUR_OF_DAY, 0)
                c.set(Calendar.MINUTE, 0)
                c.set(Calendar.SECOND, 0)

                var startDate = c.timeInMillis
                c.add(Calendar.MONTH, 1)
                var endDate = c.timeInMillis

                /*
                if (startDate < queryStartDate) {
                    startDate = queryStartDate
                }
                if (endDate > queryEndDate) {
                    endDate = queryEndDate
                }
                */

                processedBuckets[dataPointKey] = ProcessedBucket(
                    startDate,
                    endDate,
                    mutableListOf(),
                    mutableListOf(),
                    format.format(startDate),
                    format.format(endDate)
                )
                bucketKeyQueue.push(dataPointKey)
                currentCalendar.add(Calendar.MONTH, 1)
            }

            dataPoints.forEach { dataPoint ->
                val monthNumber = getMonth(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val yearNumber = getYear(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val dataPointKey = "$monthNumber$yearNumber"
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

            val currentCalendar = Calendar.getInstance()
            currentCalendar.time = Date(queryStartDate)
            while(currentCalendar.timeInMillis <= queryEndDate) {

                val yearNumber = getYear(currentCalendar.timeInMillis)
                val dataPointKey = "$yearNumber"

                val c = Calendar.getInstance()
                c.set(Calendar.YEAR, yearNumber)
                c.set(Calendar.DAY_OF_YEAR, 1)
                c[Calendar.HOUR_OF_DAY] = 0
                c[Calendar.MINUTE] = 0
                c[Calendar.SECOND] = 0

                var startDate = c.timeInMillis
                c.add(Calendar.YEAR, 1)
                var endDate = c.timeInMillis

                /*
                if (startDate < queryStartDate) {
                    startDate = queryStartDate
                }
                if (endDate > queryEndDate) {
                    endDate = queryEndDate
                }
                */

                bucketKeyQueue.push(dataPointKey)
                processedBuckets[dataPointKey] = ProcessedBucket(
                    startDate,
                    endDate,
                    mutableListOf(),
                    mutableListOf(),
                    format.format(startDate),
                    format.format(endDate)
                )
                currentCalendar.add(Calendar.YEAR, 1)
            }

            dataPoints.forEach { dataPoint ->
                val yearNumber = getYear(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                val dataPointKey = "$yearNumber"
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
                for(pointIndex in bucket.dataPoints.indices) {

                    val dataPoint = bucket.dataPoints[pointIndex]

                    variable.fields.forEach { field ->
                        if(!resultPerField.containsKey(field.name)) {
                            val results = mutableListOf<Float>()
                            resultPerField[field.name] = results
                            if(operationType != EnumOperationType.RAW.value) {
                                results.add(0F)
                            }
                        }

                        val dataPointValue = dataPoint.getValue(field).toString().toFloat()

                        when(operationType) {

                            EnumOperationType.RAW.value -> {
                                nResultsPerField += 1
                                resultPerField[field.name]!!.add(dataPointValue)
                            }

                            EnumOperationType.SUM.value -> {
                                nResultsPerField = 1
                                resultPerField[field.name]!![0] = resultPerField[field.name]!![0] + dataPointValue
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
                                nResultsPerField = 1
                                resultPerField[field.name]!![0] = resultPerField[field.name]!![0] + dataPointValue
                                if(pointIndex == bucket.dataPoints.size - 1){
                                    resultPerField[field.name]!![0] = resultPerField[field.name]!![0] / bucket.dataPoints.size
                                }
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


        private fun getSecondNumber(milliseconds: Long): Int {
            val isoCalendar = Calendar.getInstance()
            isoCalendar.time = Date(milliseconds)
            return isoCalendar.get(Calendar.SECOND)
        }
        private fun getMinuteNumber(milliseconds: Long): Int {
            val isoCalendar = Calendar.getInstance()
            isoCalendar.time = Date(milliseconds)
            return isoCalendar.get(Calendar.MINUTE)
        }
        private fun getHourNumber(milliseconds: Long): Int {
            val isoCalendar = Calendar.getInstance()
            isoCalendar.time = Date(milliseconds)
            return isoCalendar.get(Calendar.HOUR_OF_DAY)
        }
        private fun getDayNumber(milliseconds: Long): Int {
            val isoCalendar = Calendar.getInstance()
            isoCalendar.time = Date(milliseconds)
            return isoCalendar.get(Calendar.DAY_OF_MONTH)
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