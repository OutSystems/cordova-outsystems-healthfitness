package com.outsystems.plugins.healthfitness

enum class HealthFitnessError(val code: Int, val message: String) {
    // Plugin Shared errors
    VARIABLE_NOT_AVAILABLE_ERROR(100, "Variable not available."),
    VARIABLE_NOT_AUTHORIZED_ERROR(101, "Variable not authorized."),
    OPERATION_NOT_ALLOWED(102, "Operation not allowed."),
    READ_DATA_ERROR(103, "Error while reading data."),
    WRITE_DATA_ERROR(104, "Error while writing data."),
    BACKGROUND_JOB_ALREADY_EXISTS_ERROR(105, "The background job you are trying to set already exists."),
    BACKGROUND_JOB_GENERIC_ERROR(106, "The background job could not be created."),
    BACKGROUND_JOB_DOES_NOT_EXISTS_ERROR(107, "The background job could not be found."),
    UNSUBSCRIBE_ERROR(108, "The background job could not be deleted"),
    LIST_BACKGROUND_JOBS_GENERIC_ERROR(110, "The list of background jobs could not be fetched."),
    DELETE_BACKGROUND_JOB_GENERIC_ERROR(111, "The background job could not be deleted."),
    UPDATE_BACKGROUND_JOB_GENERIC_ERROR(112, "The background job could not be updated."),


    // Plugin Android specific errors
    WRITE_VALUE_OUT_OF_RANGE_ERROR(109, "Value provided is out of range for this variable"),

    // Overall Android specific
    GOOGLE_SERVICES_RESOLVABLE_ERROR (200, "It looks like Google Play services aren't available on your device."),
    GOOGLE_SERVICES_ERROR (201, "Google Play Services error."),
}