package com.outsystems.plugins.healthfitness.store

import com.google.gson.annotations.SerializedName

data class AdditionalData (
    @SerializedName("Type") var type : String,
    @SerializedName("Value") val value : String
)