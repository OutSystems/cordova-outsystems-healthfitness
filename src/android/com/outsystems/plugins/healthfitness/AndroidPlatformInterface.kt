package com.outsystems.plugins.healthfitness

import android.app.Activity
import android.content.Context
import org.apache.cordova.CallbackContext

interface AndroidPlatformInterface {

    fun getContext(): Context
    fun getActivity(): Activity

    fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray)

    fun <T> sendPluginResult(resultVariable: T, error: Pair<Int, String>? = null)
    fun areGooglePlayServicesAvailable(callbackContext: CallbackContext): Boolean

}