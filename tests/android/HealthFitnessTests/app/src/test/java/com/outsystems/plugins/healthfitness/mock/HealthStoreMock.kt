package com.outsystems.plugins.healthfitness.mock

import com.outsystems.plugins.healthfitnesslib.HealthFitnessError
import com.outsystems.plugins.healthfitnesslib.store.AdvancedQueryParameters
import com.outsystems.plugins.healthfitnesslib.store.AdvancedQueryResponse
import com.outsystems.plugins.healthfitnesslib.store.GoogleFitVariable
import com.outsystems.plugins.healthfitnesslib.store.HealthStoreInterface

class HealthStoreMock: HealthStoreInterface {

    val advancedQuerySuccess: Boolean = true
    var advancedQueryResponseForVariable: Map<String, AdvancedQueryResponse> = mapOf()

    override fun getVariableByName(name : String) : GoogleFitVariable? {
        return null
    }
    override fun advancedQueryAsync(parameters : AdvancedQueryParameters,
                           onSuccess : (AdvancedQueryResponse) -> Unit,
                           onError : (HealthFitnessError) -> Unit) {

        if(advancedQuerySuccess &&
            advancedQueryResponseForVariable.containsKey(parameters.variable)){
            onSuccess(advancedQueryResponseForVariable[parameters.variable]!!)
        }
    }
}