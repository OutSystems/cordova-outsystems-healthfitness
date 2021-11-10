package com.outsystems.plugins.healthfitnesslib.store

import com.outsystems.plugins.healthfitness.HealthFitnessError

data class AdvancedQueryResponse(
    val results : List<AdvancedQueryResponseBlock>,
    var metadata : String? = ""
)