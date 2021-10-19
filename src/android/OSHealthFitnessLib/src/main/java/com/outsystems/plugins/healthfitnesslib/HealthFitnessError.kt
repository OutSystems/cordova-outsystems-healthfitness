package com.outsystems.plugins.healthfitnesslib

enum class HealthFitnessError(val code: Int, val message: String) {
    // Plugin Shared errors
    VARIABLE_NOT_AVAILABLE_ERROR(100, "Variable not available."),
    VARIABLE_NOT_AUTHORIZED_ERROR(101, "Variable not authorized."),
    OPERATION_NOT_ALLOWED(102, "Operation not allowed."),
    READ_DATA_ERROR(103, "Error while reading data."),
    WRITE_DATA_ERROR(104, "Error while writing data."),

    // Plugin Android specific errors
    WRITE_VALUE_OUT_OF_RANGE_ERROR(109, "Value provided is out of range for this variable"),

    // Overall Android specific
    GOOGLE_SERVICES_RESOLVABLE_ERROR (200, "It looks like Google Play services aren't available on your device."),
    GOOGLE_SERVICES_ERROR (201, "Google Play Services error."),
}