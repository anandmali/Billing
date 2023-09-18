package com.anandmali.billing

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.anandmali.billing.ui.theme.BillingTheme

class MainActivity : ComponentActivity() {

    private val _connectionState = MutableLiveData(false)
    private val connectionState: LiveData<Boolean> = _connectionState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Initialize billing client wrapper
        val wrapper = BillingClientWrapper(this)
        wrapper.startConnection(_connectionState)

        setContent {
            BillingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(this, wrapper, connectionState)
                }
            }
        }
    }
}

@Composable
private fun MainScreen(
    activity: MainActivity,
    wrapper: BillingClientWrapper,
    billingConnectionState: LiveData<Boolean>
) {

    val isConnecting by billingConnectionState.observeAsState()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isConnecting == true) {
            PurchaseButton(activity, wrapper)
        } else {
            ConnectingView()
        }
    }
}

@Composable
fun ConnectingView() {
    Text(text = "Connecting to Billing Client ...")
}

@Composable
fun PurchaseButton(mainActivity: MainActivity, wrapper: BillingClientWrapper) {
    Button(onClick = {
        purchaseProduct(wrapper, mainActivity)
    }) {
        Text(text = "Purchase")
    }
}

fun purchaseProduct(wrapper: BillingClientWrapper, mainActivity: MainActivity) {
    wrapper.queryProductDetailsParams(mainActivity)
}
