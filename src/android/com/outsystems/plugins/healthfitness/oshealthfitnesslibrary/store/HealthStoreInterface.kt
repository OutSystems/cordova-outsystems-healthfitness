package com.outsystems.plugins.healthfitnesslib.store

import com.outsystems.plugins.healthfitnesslib.HealthFitnessError

interface HealthStoreInterface {

    fun getVariableByName(name : String) : GoogleFitVariable?
    fun advancedQueryAsync(parameters : AdvancedQueryParameters,
                           onSuccess : (AdvancedQueryResponse) -> Unit,
                           onError : (HealthFitnessError) -> Unit)
}