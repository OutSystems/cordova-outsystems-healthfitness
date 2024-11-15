package com.outsystems.plugins.healthfitness

import com.google.gson.annotations.SerializedName
import com.outsystems.plugins.healthfitness.data.types.HealthAdvancedQueryResponse
import org.json.JSONObject

enum class OSHealthFitnessWarning(val code: Int, val message: String) {
    DEPRECATED_TIME_UNIT(405,
        "The TimeUnit parameters of type MILLISECONDS or SECONDS are deprecated on Android. By default, TimeUnit will be set to MINUTE.");
}

data class OSHFPluginResponse(
    @SerializedName("code")
    private val code: Int,
    @SerializedName("message")
    private val message: String
)