package com.outsystems.plugins.healthfitness

import com.google.gson.Gson
import com.outsystems.plugins.healthfitness.mock.AndroidPlatformMock
import com.outsystems.plugins.healthfitness.mock.DatabaseManagerMock
import com.outsystems.plugins.healthfitness.mock.HealthFitnessManagerMock
import com.outsystems.plugins.healthfitness.HealthFitnessError
import com.outsystems.plugins.healthfitness.store.GoogleFitGroupPermission
import com.outsystems.plugins.healthfitness.store.GoogleFitPermission
import com.outsystems.plugins.healthfitness.store.HealthStore
import com.outsystems.plugins.healthfitness.store.HealthStoreException
import org.junit.Assert
import org.junit.Test

class RequestPermissionsTest {

    private val gson = Gson()
    private val groupPermissions = GoogleFitGroupPermission(false, "READ")
    private val groupPermissionsJson = gson.toJson(groupPermissions)

    @Test
    fun given_InvalidVariable_When_RequestingPermissions_Then_VariableNotAvailableError() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)

        val customPermissions = arrayOf(GoogleFitPermission("Test", "READ"))
        val customPermissionsJson = gson.toJson(customPermissions)

        try {
            store.initAndRequestPermissions(
                customPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson
            )
            Assert.fail()
        }
        catch(hse : HealthStoreException) {
            Assert.assertEquals(hse.error.code, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.code)
            Assert.assertEquals(hse.error.message, HealthFitnessError.VARIABLE_NOT_AVAILABLE_ERROR.message)
        }
    }

    @Test
    fun given_PermissionsGranted_When_RequestingPermissions_Then_Success() {
        val googleFitMock = HealthFitnessManagerMock()
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)
        val customPermissions = arrayOf(GoogleFitPermission("HEART_RATE", "READ"))
        val customPermissionsJson = gson.toJson(customPermissions)

        try {
            store.initAndRequestPermissions(
                customPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson
            )
            store.requestGoogleFitPermissions()
        }
        catch(hse: HealthStoreException) {
            Assert.fail()
        }
    }

    @Test
    fun given_PermissionsNotGranted_When_RequestingPermissions_UserDenies_Then_VariableNotAuthorizedError() {
        val googleFitMock = HealthFitnessManagerMock().apply {
            permissionsGranted = false
            permissionsGrantedOnRequest = false
        }
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)
        googleFitMock.store = store // This is a bit of a hack so the store code is tested.

        val customPermissions = arrayOf(GoogleFitPermission("HEART_RATE", "READ"))
        val customPermissionsJson = gson.toJson(customPermissions)

        try {
            store.initAndRequestPermissions(
                customPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson
            )
            store.requestGoogleFitPermissions()
            Assert.fail()
        }
        catch(hse : HealthStoreException) {
            Assert.assertEquals(hse.error.code, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.code)
            Assert.assertEquals(hse.error.message, HealthFitnessError.VARIABLE_NOT_AUTHORIZED_ERROR.message)
        }
    }

    @Test
    fun given_PermissionsNotGranted_When_RequestingPermissions_UserGrants_Then_Success()  {

        val googleFitMock = HealthFitnessManagerMock().apply {
            permissionsGranted = false
            permissionsGrantedOnRequest = true
        }
        val databaseMock = DatabaseManagerMock()
        val store = HealthStore("", googleFitMock, databaseMock)
        googleFitMock.store = store // This is a bit of a hack so the store code is tested.

        val customPermissions = arrayOf(GoogleFitPermission("HEART_RATE", "READ"))
        val customPermissionsJson = gson.toJson(customPermissions)

        try {
            store.initAndRequestPermissions(
                customPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson,
                groupPermissionsJson
            )
            store.requestGoogleFitPermissions()
        }
        catch (hse : HealthStoreException) {
            Assert.fail()
        }
    }

}