package com.outsystems.plugins.healthfitness

enum class HealthFitnessError(val code: Int, val message: String) {

    GOOGLE_SERVICES_ERROR_RESOLVABLE (100, "Google Play Services error user resolvable"),
    GOOGLE_SERVICES_ERROR (101, "Google Play Services error"),
    WRITE_VALUE_INCORRECT_FORMAT_ERROR(102, "Value provided is not in correct format"),
    WRITE_VALUE_OUT_OF_RANGE_ERROR(103, "Value provided is out of range for this variable"),
    WRITE_DATA_ERROR(104, "Error while writing data"),
    READ_DATA_ERROR(105, "Error while reading data")
}