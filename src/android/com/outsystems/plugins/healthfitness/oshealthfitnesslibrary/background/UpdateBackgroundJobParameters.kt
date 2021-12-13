package com.outsystems.plugins.healthfitness.background

import com.google.gson.annotations.SerializedName

data class UpdateBackgroundJobParameters (
    @SerializedName("Id") val id : String,
    @SerializedName("Value") val value : Float?,
    @SerializedName("Comparison") val comparison : String?,
    @SerializedName("NotificationFrequency") val notificationFrequency : String?,
    @SerializedName("NotificationFrequencyGrouping") val notificationFrequencyGrouping : Int?,
    @SerializedName("IsActive") val isActive : Boolean?,
    @SerializedName("NotificationHeader") val notificationHeader : String?,
    @SerializedName("NotificationBody") val notificationBody : String?
)