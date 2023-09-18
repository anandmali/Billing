package com.anandmali.billing

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList

class BillingClientWrapper(
    context: Context
) : ProductDetailsResponseListener, PurchasesUpdatedListener {

    private var activity: Activity? = null

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .build()

    fun startConnection(onConnect: MutableLiveData<Boolean>) {

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    onConnect.postValue(true)
                }
            }

            override fun onBillingServiceDisconnected() {
                startConnection(onConnect)
            }
        })
    }

    fun queryProductDetailsParams(
        activity: Activity
    ) {

        this.activity = activity
        val iapProducts = ImmutableList.of(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("id_1")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(iapProducts)
                .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams, this)

    }

    override fun onProductDetailsResponse(
        billingResult: BillingResult,
        productDetailsList: MutableList<ProductDetails>
    ) {

        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {

                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetailsList[0])
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                if (billingClient.isReady) {
                    billingClient.launchBillingFlow(this.activity!!, billingFlowParams)
                }
            }

            else -> {
                //Handle else
            }
        }
    }

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
        //Handle purchase updates
    }
}