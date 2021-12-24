package com.outsystems.plugins.healthfitness.store

import com.outsystems.plugins.healthfitness.HealthFitnessError

data class AdvancedQueryResponse(
    val results : List<AdvancedQueryResponseBlock>,
    var metadata : String? = ""
)