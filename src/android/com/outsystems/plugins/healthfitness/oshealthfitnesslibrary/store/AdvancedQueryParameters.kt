package com.outsystems.plugins.healthfitnesslib.store

import com.google.gson.annotations.SerializedName
import java.util.*

data class AdvancedQueryParameters (
    @SerializedName("Variable") val variable : String,
    @SerializedName("StartDate") val startDate : Date,
    @SerializedName("EndDate") val endDate : Date,
    @SerializedName("TimeUnit") val timeUnit : String? = null,
    @SerializedName("TimeUnitLength") var timeUnitLength : Int? = 1,
    @SerializedName("OperationType") val operationType : String? = EnumOperationType.RAW.value,
    @SerializedName("Limit") val limit : Int? = null
)