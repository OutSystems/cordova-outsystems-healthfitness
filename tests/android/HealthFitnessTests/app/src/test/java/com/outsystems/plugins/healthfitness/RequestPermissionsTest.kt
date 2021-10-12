package com.outsystems.plugins.healthfitness

import com.outsystems.plugins.healthfitness.store.HealthStore
import org.junit.Test

class RequestPermissionsTest {

    @Test
    fun given_InvalidVariable_When_RequestingPermissions_Then_VariableNotAvailableError() {
        val platformInterfaceMock = AndroidPlatformMock()
        val googleFitMock = HealthFitnessManagerMock()
        val store = HealthStore(platformInterfaceMock, googleFitMock)

        //store.initAndRequestPermissions()
    }

    @Test
    fun given_PermissionsDenied_When_RequestingPermissions_Then_VariableNotAuthorizedError() {
        //TODO
    }

    @Test
    fun given_PermissionsGranted_When_RequestingPermissions_Then_Success() {
        //TODO
    }

}