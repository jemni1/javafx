package edu.connection.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import edu.connection.utils.StripeConfig;

public class StripeService {

    public StripeService() {
        Stripe.apiKey = StripeConfig.getSecretKey(); // Configure ta clé secrète Stripe
    }

    public PaymentIntent createPaymentIntent(double amountEuros) throws StripeException {
        long amountInCents = (long) (amountEuros * 100); // Stripe attend les montants en centimes

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
                        .setCurrency("eur")
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                        )
                        .build();

        return PaymentIntent.create(params);
    }
}
