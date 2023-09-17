package com.anandmali.billing

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anandmali.billing.ui.theme.BillingTheme
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList

// List of subscription product offerings
private const val BASIC_SUB = "up_basic_sub"
private const val PREMIUM_SUB = "up_premium_sub"

private val LIST_OF_PRODUCTS = listOf(BASIC_SUB, PREMIUM_SUB)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BillingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PurchaseButton(this@MainActivity)
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseButton(mainActivity: MainActivity) {
    val context = LocalContext.current
    Button(onClick = {
        purchaseProduct(context, mainActivity)
    }) {
        Text(text = "Purchase")
    }
}

fun purchaseProduct(context: Context, mainActivity: MainActivity) {
    val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
        }

    val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    billingClient.startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // The BillingClient is ready. You can query purchases here.
            }
        }

        override fun onBillingServiceDisconnected() {
            // Try to restart the connection on the next request to
            // Google Play by calling the startConnection() method.
        }
    })

    val queryProductDetailsParams =
        QueryProductDetailsParams.newBuilder()
            .setProductList(
                ImmutableList.of(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("product_id_example")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

    var prodList: MutableList<ProductDetails>? = null
    billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
        // check billingResult
        // process returned productDetailsList
        prodList = productDetailsList
    }

    val productDetailsParamsList = listOf(
        BillingFlowParams.ProductDetailsParams.newBuilder()
            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
            .setProductDetails(prodList!![0])
            // to get an offer token, call ProductDetails.subscriptionOfferDetails()
            // for a list of offers that are available to the user
            .setOfferToken("selectedOfferToken")
            .build()
    )

    val billingFlowParams = BillingFlowParams.newBuilder()
        .setProductDetailsParamsList(productDetailsParamsList)
        .build()

    // Launch the billing flow
    val billingResult = billingClient.launchBillingFlow(mainActivity, billingFlowParams)

}
