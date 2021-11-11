package com.outsystems.plugins.healthfitnesslib.store

data class AdvancedQueryResponseBlock (
    val block : Int,
    val startDate : Long,
    val endDate : Long,
    val values : MutableList<Float>
)