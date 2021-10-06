package com.outsystems.plugins.healthfitness.store

data class AdvancedQueryResponseBlock (
    val block : Int,
    val startDate : Long,
    val endDate : Long,
    val values : MutableList<Float>
)