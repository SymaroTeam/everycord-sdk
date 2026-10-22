package net.everycord.sample

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import net.everycord.sdk.EveryCord

/**
 * The app's own Play Billing, as it already is, plus two lines for EveryCord's earnings cross-check: the purchases
 * listener hands its purchases to [EveryCord.earnings], and the connected client is attached once so existing purchases
 * and prices are read. The SDK only reads them; acknowledging and consuming stay the app's job.
 */
object Billing {
    /** Kept for the app's lifetime (the SDK holds it only weakly). */
    private var client: BillingClient? = null

    fun start(context: Context) {
        val billing = BillingClient.newBuilder(context)
            .setListener { _, purchases ->
                // Your own handling first (grant, acknowledge, consume), then:
                EveryCord.earnings.onPurchasesUpdated(purchases)
            }
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection()
            .build()
        billing.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) EveryCord.earnings.attach(billing)
            }
            override fun onBillingServiceDisconnected() {}
        })
        client = billing
    }
}
