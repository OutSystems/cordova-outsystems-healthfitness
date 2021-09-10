package com.outsystems.plugins.healthfitness

enum class HealthFitnessError(val code: Int, val message: String) {

    GOOGLE_SERVICES_ERROR_RESOLVABLE (100, "Google Play Services error user resolvable"),
    GOOGLE_SERVICES_ERROR (101, "Google Play Services error");

}