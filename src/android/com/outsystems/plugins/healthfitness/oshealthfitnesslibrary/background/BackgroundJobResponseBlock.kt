package com.outsystems.plugins.healthfitness.background

data class BackgroundJobsResponseBlock (
    val variable: String?,
    val condition: String?,
    val value: Float?,
    val notificationHeader: String?,
    val notificationBody: String?,
    val notificationFrequency: String?,
    val notificationFrequencyGrouping: Int?,
    val active: Boolean?,
    val id: String?
)