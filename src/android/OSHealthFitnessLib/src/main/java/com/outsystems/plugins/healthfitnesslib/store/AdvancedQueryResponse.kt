package com.outsystems.plugins.healthfitnesslib.store

data class AdvancedQueryResponse(
    val results : List<AdvancedQueryResponseBlock>,
    var metadata : String? = ""
)