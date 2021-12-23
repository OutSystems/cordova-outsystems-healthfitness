package com.outsystems.plugins.healthfitness.background

import com.google.gson.annotations.SerializedName

data class BackgroundJobParameters (
    @SerializedName("Variable") val variable : String,
    @SerializedName("Value") val value : String,
    @SerializedName("Condition") val condition : String,
    @SerializedName("TimeUnit") val timeUnit : String? = null,
    @SerializedName("TimeUnitGrouping") val timeUnitGrouping : Int? = 1,
    @SerializedName("JobFrequency") val jobFrequency : String? = null,
    @SerializedName("NotificationFrequency") val notificationFrequency : String? = null,
    @SerializedName("NotificationFrequencyGrouping") val notificationFrequencyGrouping : Int? = 1,
    @SerializedName("NotificationHeader") val notificationHeader : String,
    @SerializedName("NotificationBody") val notificationBody : String
)