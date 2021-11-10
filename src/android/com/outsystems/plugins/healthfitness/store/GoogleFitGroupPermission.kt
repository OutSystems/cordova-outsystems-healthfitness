package com.outsystems.plugins.healthfitnesslib.store

import com.google.gson.annotations.SerializedName

data class GoogleFitGroupPermission (
    @SerializedName("IsActive") val isActive : Boolean = false,
    @SerializedName("AccessType") val accessType : String = ""
)
