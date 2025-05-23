package edu.connection.controllers;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import io.github.cdimascio.dotenv.Dotenv;

public class processPayment {

private void processPayment() {
    try {
// Set your secret key here
        Dotenv dotenv = Dotenv.load();
        Stripe.apiKey = dotenv.get("STRIPE_API_KEY");
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
