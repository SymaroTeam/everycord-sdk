package net.everycord.sample

import android.app.Application
import android.util.Log
import net.everycord.sdk.EveryCord

/** Configures the SDK once, at launch, with the app's publishable key. */
class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            val apiBaseUrl = BuildConfig.EVERYCORD_API_BASE_URL
            if (apiBaseUrl.isEmpty()) EveryCord.configure(this, BuildConfig.EVERYCORD_PUBLISHABLE_KEY)
            else EveryCord.configure(this, BuildConfig.EVERYCORD_PUBLISHABLE_KEY, apiBaseUrl)
            Billing.start(this)
        } catch (e: IllegalArgumentException) {
            // Not a publishable key: set everycord.publishableKey in gradle.properties.
            Log.e("EveryCordSample", e.message.orEmpty())
        }
    }
}
