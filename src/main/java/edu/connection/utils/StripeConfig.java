package edu.connection.utils;

public class StripeConfig {
    // Utilisez la clé de test pour le développement

    public static String getSecretKey() {
        return API_SECRET_KEY;
    }

    public static String getPublicKey() {
        return API_PUBLIC_KEY;
    }
}