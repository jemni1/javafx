package edu.connection.controllers;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

public class processPayment {

private void processPayment() {
    try {
// Set your secret key here
        Stripe.apiKey = "sk_test_51QqfdtK8cuBxoUKZIk6DsmePaibHWa4h1yavyEVspikhWuuT7TmEYeGDm3mWD6ODGKAkPor6MzMmFEwezbkE39pm00q90TweRy";

// Create a PaymentIntent with other payment details
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(1000L) // Amount in cents (e.g., $10.00)
                .setCurrency("usd")
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

// If the payment was successful, display a success message
        System.out.println("Payment successful. PaymentIntent ID: " + intent.getId());
    } catch (StripeException e) {
// If there was an error processing the payment, display the error message
        System.out.println("Payment failed. Error: " + e.getMessage());
    }
}}
