package com.outsystems.plugins.healthfitness

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.outsystems.plugins.healthfitness.store.HealthStore

import org.apache.cordova.*

import org.json.JSONArray


enum class EnumPermissionAccess {
    GRANTED,
    DENIED,
    FULLY_DENIED
}

class OSHealthFitness : CordovaImplementation() {
    override var callbackContext: CallbackContext? = null

    var healthStore: HealthStore? = null

    override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
        super.initialize(cordova, webView)
        healthStore = HealthStore(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        this.callbackContext = callbackContext
        when (action) {
            "requestPermissions" -> {
                initAndRequestPermissions(args)
            }
            "getData" -> {
                getData()
            }
        }
        return true
    }

    //create array of permission oauth

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initAndRequestPermissions(args : JSONArray) {
        healthStore?.initAndRequestPermissions(args)
        checkAndGrantPermissions()
    }

    private fun checkAllPermissionGranted(permissions: Array<String>): Boolean {
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(
                    cordova.activity,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun checkAllGoogleFitPermissionGranted(): Boolean {
       return healthStore!!.checkAllGoogleFitPermissionGranted()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun checkAndGrantPermissions(): Boolean? {
        val permissions = arrayOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BODY_SENSORS
        )
        if (checkAllPermissionGranted(permissions)) {
            healthStore?.requestGoogleFitPermissions()
        } else {
            PermissionHelper.requestPermissions(
                this,
                ACTIVITY_LOCATION_PERMISSIONS_REQUEST_CODE,
                permissions
            )
        }
        return false
    }

    //Get steps by day
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun getData() {
        healthStore!!.getData()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> {
                healthStore!!.getData()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            ACTIVITY_LOCATION_PERMISSIONS_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    checkAndGrantPermissions()
                } else {
                }
                return
            }
        }
    }

    companion object {
        const val ACTIVITY_LOCATION_PERMISSIONS_REQUEST_CODE = 1
        const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 2
    }
}