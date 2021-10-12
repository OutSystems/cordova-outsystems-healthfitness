package com.outsystems.plugins.healthfitness

import android.app.Activity
import android.content.Context
import org.apache.cordova.CallbackContext

class AndroidInterfaceMock : AndroidPlatformInterface {

    override fun getContext(): Context {
        TODO("Not yet implemented")
    }

    override fun getActivity(): Activity {
        TODO("Not yet implemented")
    }

    override fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        TODO("Not yet implemented")
    }

    override fun <T> sendPluginResult(resultVariable: T, error: Pair<Int, String>?) {
        TODO("Not yet implemented")
    }

    override fun areGooglePlayServicesAvailable(callbackContext: CallbackContext): Boolean {
        TODO("Not yet implemented")
    }

}