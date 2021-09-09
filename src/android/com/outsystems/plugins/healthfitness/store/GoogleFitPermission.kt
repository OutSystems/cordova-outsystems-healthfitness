package com.outsystems.plugins.healthfitness.store

import com.google.gson.annotations.SerializedName

data class GoogleFitPermission(
    @SerializedName("Variable") val variable : String,
    @SerializedName("AccessType") val accessType : String
)