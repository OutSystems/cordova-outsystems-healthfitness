package com.outsystems.plugins.healthfitness.store

import java.util.*

data class AdvancedQueryResponseBlock (
    val block : Int,
    val startDate : String,
    val endDate : String,
    val values : List<Float>
)