package com.outsystems.plugins.healthfitness

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.fitness.data.DataType
import org.apache.cordova.*
import org.json.JSONArray
import org.json.JSONObject

abstract  class CordovaImplementation : CordovaPlugin(), AndroidPlatformInterface {

    abstract var callbackContext: CallbackContext?



    abstract override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean

    override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
        super.initialize(cordova, webView)
    }

     override fun getContext(): Context {
        return cordova.context
    }

     override fun getActivity(): Activity {
        return cordova.activity
    }

     fun onPermissionResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionResult(requestCode,permissions,grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  intent: Intent
    ) {
        super.onActivityResult(requestCode,resultCode,intent)
    }

    override fun <T> sendPluginResult(resultVariable: T, error: Pair<Int, String>?) {
        var pluginResult: PluginResult? = null
        resultVariable?.let {
            val jsonResult = JSONObject()
            jsonResult.put("result", resultVariable)
            pluginResult = PluginResult(PluginResult.Status.OK, jsonResult)
            this.callbackContext?.let {
                it.sendPluginResult(pluginResult)
            }
            return
        }
        val jsonResult = JSONObject()
        jsonResult.put("code", error?.first)
        jsonResult.put("message", error?.second ?: "No Results")
        pluginResult = PluginResult(PluginResult.Status.ERROR, jsonResult)
        this.callbackContext?.let {
            it.sendPluginResult(pluginResult)
        }
    }

    abstract override fun areGooglePlayServicesAvailable(callbackContext: CallbackContext): Boolean

}