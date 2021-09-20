package com.outsystems.plugins.healthfitness.store

import java.util.*

data class AdvancedQueryResponseBlock (
    val block : Int,
    val startDate : Long,
    val endDate : Long,
    val values : List<Float>
)