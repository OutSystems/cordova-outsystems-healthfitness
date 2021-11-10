package com.outsystems.plugins.healthfitnesslib.background

import com.google.gson.annotations.SerializedName

data class BackgroundJobParameters (
    @SerializedName("Variable") val variable : String,
    @SerializedName("Value") val value : String,
    @SerializedName("Condition") val condition : String,
    @SerializedName("TimeUnit") val timeUnit : String? = null,
    @SerializedName("TimeUnitGrouping") val timeUnitGrouping : Int? = 1,
    @SerializedName("JobFrequency") val jobFrequency : String? = null,
    @SerializedName("NotificationHeader") val notificationHeader : String,
    @SerializedName("NotificationBody") val notificationBody : String
)