package com.outsystems.plugins.healthfitness.store

import com.google.gson.annotations.SerializedName
import java.util.*

data class AdvancedQueryParameters (
    @SerializedName("Variable") var variable : String,
    @SerializedName("StartDate") val startDate : Date,
    @SerializedName("EndDate") val endDate : Date,
    @SerializedName("TimeUnit") var timeUnit : String? = null,
    @SerializedName("TimeUnitLength") var timeUnitLength : Int? = 1,
    @SerializedName("OperationType") var operationType : String? = EnumOperationType.RAW.value,
    @SerializedName("Limit") val limit : Int? = null
)