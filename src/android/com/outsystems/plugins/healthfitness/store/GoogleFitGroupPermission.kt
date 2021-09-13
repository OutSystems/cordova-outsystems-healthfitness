package com.outsystems.plugins.healthfitness.store

import com.google.gson.annotations.SerializedName

class GoogleFitGroupPermission (
    @SerializedName("IsActive") val isActive : Boolean = false,
    @SerializedName("AccessType") val accessType : String = ""
)
