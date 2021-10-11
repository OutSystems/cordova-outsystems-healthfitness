package com.outsystems.plugins.healthfitness

import android.content.Context
import com.google.android.gms.fitness.FitnessOptions
import com.outsystems.plugins.healthfitness.store.HealthFitnessManagerInterface

class HealthFitnessManagerMock: HealthFitnessManagerInterface {

    override fun createAccount(context: Context, options: FitnessOptions) {
        TODO("Not yet implemented")
    }

    override fun areGoogleFitPermissionsGranted(options: FitnessOptions?): Boolean {
        TODO("Not yet implemented")
    }

}