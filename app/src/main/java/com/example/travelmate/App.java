package com.example.travelmate;

import android.app.Application;

import com.paypal.checkout.PayPalCheckout;
import com.paypal.checkout.config.CheckoutConfig;
import com.paypal.checkout.config.Environment;
import com.paypal.checkout.config.PaymentButtonIntent;
import com.paypal.checkout.config.SettingsConfig;
import com.paypal.checkout.config.UIConfig;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.UserAction;

public class App extends Application {

    public void onCreate() {
        super.onCreate();
        PayPalCheckout.setConfig(new CheckoutConfig(
                this,
                "AfDONnKDLbXIQ1aPfoSpqCzuO5wn72lG4R21WM44h5Nk8QBsRDVUyb-JVUQzPkUsAz70qk9iHeJc3PSn",
                Environment.SANDBOX,
                CurrencyCode.USD,
                UserAction.PAY_NOW,
                PaymentButtonIntent.CAPTURE,
                new SettingsConfig(true, false),
                new UIConfig(true),
                "com.example.travelmate://paypalpay"
        ));
    }
}
