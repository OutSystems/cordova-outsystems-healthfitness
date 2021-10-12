package com.outsystems.plugins.healthfitness

import android.app.Activity
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.apache.cordova.CallbackContext

class AndroidPlatformMock : AndroidPlatformInterface {

    var sendPluginResultCompletion: ((resultVariable: String, error: Pair<Int, String>?) -> Unit)? = null

    override fun getContext(): Context {
        return ApplicationProvider.getApplicationContext<Context>()
    }

    override fun getActivity(): Activity {
        return ApplicationProvider.getApplicationContext<Activity>()
    }

    override fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        TODO("Not yet implemented")
    }

    override fun <T> sendPluginResult(resultVariable: T, error: Pair<Int, String>?) {
        sendPluginResultCompletion?.let { it(resultVariable.toString(), error) }
    }

    override fun areGooglePlayServicesAvailable(callbackContext: CallbackContext): Boolean {
        TODO("Not yet implemented")
    }

}