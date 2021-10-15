package com.outsystems.plugins.healthfitness.store

import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field

data class GoogleFitVariable (
    val dataType : DataType,
    val fields : List<Field>,
    var allowedOperations : List<String>
)