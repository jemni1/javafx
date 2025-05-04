package edu.connection.utils;

public class StripeConfig {
    // Utilisez la clé de test pour le développement
    private static final String API_SECRET_KEY = "sk_test_51QqfdtK8cuBxoUKZIk6DsmePaibHWa4h1yavyEVspikhWuuT7TmEYeGDm3mWD6ODGKAkPor6MzMmFEwezbkE39pm00q90TweRy";
    private static final String API_PUBLIC_KEY = "pk_test_51QqfdtK8cuBxoUKZV1aRKVzlBVNlOzvX72hV90A1CLuIJdcP3sngvsxVFUFtTFCo8vGu5qLVXwqRD7v2n6WPZqvx00sTEz5YkT";

    public static String getSecretKey() {
        return API_SECRET_KEY;
    }

    public static String getPublicKey() {
        return API_PUBLIC_KEY;
    }
}