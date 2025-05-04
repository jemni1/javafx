package edu.connection.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class StripeConfig {
    // Utilisez la clé de test pour le développement
    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_SECRET_KEY = dotenv.get("STRIPE_API_SECRET_KEY");
    private static final String API_PUBLIC_KEY = dotenv.get("STRIPE_API_PUBLIC_KEY");
    public static String getSecretKey() {
        return API_SECRET_KEY;
    }

    public static String getPublicKey() {
        return API_PUBLIC_KEY;
    }
}