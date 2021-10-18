package com.outsystems.plugins.healthfitness

import android.app.Activity
import android.content.Context

interface AndroidPlatformInterface {

    fun getContext(): Context
    fun getActivity(): Activity
    fun getPackageAppName() : String

    fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray)

    fun <T> sendPluginResult(resultVariable: T, error: Pair<Int, String>? = null)
    fun areGooglePlayServicesAvailable(): Boolean

}