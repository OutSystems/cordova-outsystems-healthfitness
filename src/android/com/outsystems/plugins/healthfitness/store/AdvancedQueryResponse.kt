package com.outsystems.plugins.healthfitness.store

data class AdvancedQueryResponse(
    val results : List<AdvancedQueryResponseBlock>,
    var metadata : String? = ""
)