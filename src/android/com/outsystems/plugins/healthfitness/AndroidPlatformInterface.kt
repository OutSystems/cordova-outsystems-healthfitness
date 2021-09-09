package com.outsystems.plugins.healthfitness

import android.app.Activity
import android.content.Context
import android.content.Intent

interface AndroidPlatformInterface {
    fun getContext(): Context
    fun getActivity(): Activity
    fun onRequestPermissionResult( requestCode: Int, permissions: Array<String>,
                            grantResults: IntArray)
    fun <T> sendPluginResult(resultVariable: T, error: String? = null)
}