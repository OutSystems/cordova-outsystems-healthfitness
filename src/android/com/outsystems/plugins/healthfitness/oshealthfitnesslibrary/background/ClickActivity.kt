package com.outsystems.plugins.healthfitnesslib.background

import android.app.Activity
import android.content.Intent

import android.os.Bundle

import androidx.annotation.Nullable
import androidx.core.app.NotificationManagerCompat


class ClickActivity : Activity() {

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchApp()
        finish()
    }

    private fun launchApp() {
        val context = applicationContext
        val packageName = context.packageName

        val intent = context
            .packageManager
            .getLaunchIntentForPackage(packageName)

        if(intent == null)
            return

        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

}