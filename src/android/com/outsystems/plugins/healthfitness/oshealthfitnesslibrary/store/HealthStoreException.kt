package com.outsystems.plugins.healthfitness.store

import com.outsystems.plugins.healthfitness.HealthFitnessError
import java.lang.Exception

class HealthStoreException(val error : HealthFitnessError) : Exception() {

}