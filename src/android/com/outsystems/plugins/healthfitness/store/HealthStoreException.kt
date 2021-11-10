package com.outsystems.plugins.healthfitnesslib.store

import com.outsystems.plugins.healthfitnesslib.HealthFitnessError
import java.lang.Exception

class HealthStoreException(val error : HealthFitnessError) : Exception() {

}