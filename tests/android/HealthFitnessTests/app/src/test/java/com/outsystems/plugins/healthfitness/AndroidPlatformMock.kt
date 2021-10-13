package com.outsystems.plugins.healthfitness

import androidx.test.ext.junit.runners.AndroidJUnit4
import android.app.Activity
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.apache.cordova.CallbackContext
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidPlatformMock(
    var sendPluginResultCompletion: ((resultVariable: String, error: Pair<Int, String>?) -> Unit)? = null
) : AndroidPlatformInterface {

    override fun getContext(): Context {
        return ApplicationProvider.getApplicationContext() // Should not be used in Mock
    }
    override fun getActivity(): Activity {
        return Activity() // Should not be used in Mock
    }
    override fun getPackageAppName(): String {
        return "com.outsystems.plugins.healthfitness"
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

    data class Builder() {
        fun pluginResultCompletion(completion : (resultVariable: String, error: Pair<Int, String>?) -> Unit) = apply { this.sendPluginResultCompletion = completion }
        fun build() = AndroidPlatformMock(sendPluginResultCompletion)
    }

}