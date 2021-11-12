package com.outsystems.plugins.healthfitness

import com.google.gson.Gson
import com.outsystems.plugins.healthfitness.mock.AndroidPlatformMock
import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthFitnessManagerMock
import com.outsystems.plugins.healthfitnesslib.HealthFitnessError
import com.outsystems.plugins.healthfitnesslib.store.GoogleFitGroupPermission
import com.outsystems.plugins.healthfitnesslib.store.GoogleFitPermission
import com.outsystems.plugins.healthfitnesslib.store.HealthStore
import org.junit.Assert
import org.junit.Test

class RequestPermissionsTest {

    private val gson = Gson()
    private val groupPermissions = GoogleFitGroupPermission(false, "READ")
    private val groupPermissionsJson = gson.toJson(groupPermissions)

    @Test
    fun given_InvalidVariable_When_RequestingPermissions_Then_VariableNotAvailableError() {
        var wasThrownError = false
        val platformInterfaceMock = AndroidPlatformMock().apply {
            sendPluginResultCompletion = { result, error ->
                Assert.assertEquals(result, "null")
                val code = error?.first
                val message = error?.second
                Assert.assertEquals(code, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.code)
                Assert.assertEquals(message, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.message)
                wasThrownError = true
            }
        }

        val googleFitMock = HealthFitnessManagerMock()
        val store = HealthStore(platformInterfaceMock, googleFitMock)

        val customPermissions = arrayOf(GoogleFitPermission("Test", "READ"))
        val customPermissionsJson = gson.toJson(customPermissions)

        store.initAndRequestPermissions(
            customPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson
        )

        Assert.assertTrue(wasThrownError)

    }

    @Test
    fun given_PermissionsGranted_When_RequestingPermissions_Then_Success() {
        var wasSuccessCalled = false
        val platformInterfaceMock = AndroidPlatformMock().apply {
            sendPluginResultCompletion = { result, _ ->
                Assert.assertEquals(result, "success")
                wasSuccessCalled = true
            }
        }

        val googleFitMock = HealthFitnessManagerMock()
        val store = HealthStore(platformInterfaceMock, googleFitMock)

        val customPermissions = arrayOf(GoogleFitPermission("HEART_RATE", "READ"))
        val customPermissionsJson = gson.toJson(customPermissions)

        store.initAndRequestPermissions(
            customPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson
        )
        store.requestGoogleFitPermissions()

        Assert.assertTrue(wasSuccessCalled)
    }

    @Test
    fun given_PermissionsNotGranted_When_RequestingPermissions_UserDenies_Then_VariableNotAuthorizedError() {
        var wasThrownError = false
        val platformInterfaceMock = AndroidPlatformMock().apply {
            sendPluginResultCompletion = { result, error ->
                Assert.assertEquals(result, "null")
                val code = error?.first
                val message = error?.second
                Assert.assertEquals(code, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.code)
                Assert.assertEquals(message, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.message)
                wasThrownError = true
            }
        }

        val googleFitMock = HealthFitnessManagerMock().apply {
            permissionsGranted = false
            permissionsGrantedOnRequest = false
        }
        val store = HealthStore(platformInterfaceMock, googleFitMock)
        googleFitMock.store = store // This is a bit of a hack so the store code is tested.

        val customPermissions = arrayOf(GoogleFitPermission("HEART_RATE", "READ"))
        val customPermissionsJson = gson.toJson(customPermissions)

        store.initAndRequestPermissions(
            customPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson
        )
        store.requestGoogleFitPermissions()

        Assert.assertTrue(wasThrownError)
    }

    @Test
    fun given_PermissionsNotGranted_When_RequestingPermissions_UserGrants_Then_Success() {
        var wasSuccessCalled = false
        val platformInterfaceMock = AndroidPlatformMock().apply {
            sendPluginResultCompletion = { result, _ ->
                Assert.assertEquals(result, "success")
                wasSuccessCalled = true
            }
        }

        val googleFitMock = HealthFitnessManagerMock().apply {
            permissionsGranted = false
            permissionsGrantedOnRequest = true
        }
        val store = HealthStore(platformInterfaceMock, googleFitMock)
        googleFitMock.store = store // This is a bit of a hack so the store code is tested.

        val customPermissions = arrayOf(GoogleFitPermission("HEART_RATE", "READ"))
        val customPermissionsJson = gson.toJson(customPermissions)

        store.initAndRequestPermissions(
            customPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson,
            groupPermissionsJson
        )
        store.requestGoogleFitPermissions()

        Assert.assertTrue(wasSuccessCalled)
    }

}