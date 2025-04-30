package edu.connexion3a41.App;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class RecaptchaService {
    private static final String SITE_KEY = "6Lf_FxsrAAAAACI6WbkWJqg8tYs4olMKBe467vZV";
    private static final String SECRET_KEY = "6Lf_FxsrAAAAAFBvx76jLpJkthYbnWxoxkIZ9ZoR";
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public void loadRecaptchaWidget(WebView webView, CompletableFuture<String> tokenFuture, String action) {
        WebEngine engine = webView.getEngine();
        engine.loadContent(""); // Clear previous content

        // Error handling
        engine.setOnError(event -> {
            System.err.println("WebView Error: " + event.getMessage());
        });

        engine.getLoadWorker().exceptionProperty().addListener((obs, old, ex) -> {
            if (ex != null) {
                System.err.println("Load Exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // reCAPTCHA v2 HTML
        String htmlContent = String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "   <script src='https://www.google.com/recaptcha/api.js'></script>" +
                        "   <script>" +
                        "       function onRecaptchaSuccess(token) {" +
                        "           window.javaCallback.processToken(token);" +
                        "       }" +
                        "   </script>" +
                        "</head>" +
                        "<body style='margin:0;padding:0;background:transparent;'>" +
                        "   <div class='g-recaptcha' " +
                        "        data-sitekey='%s' " +
                        "        data-callback='onRecaptchaSuccess' " +
                        "        data-size='normal'></div>" +
                        "</body>" +
                        "</html>", SITE_KEY);

        engine.loadContent(htmlContent);

        // Set up Java-JS bridge
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("javaCallback", new RecaptchaCallback(tokenFuture));
                } catch (Exception e) {
                    System.err.println("Error setting up Java callback: " + e.getMessage());
                }
            }
        });
    }

    public boolean verifyRecaptchaToken(String token) {
        try {
            URL url = new URL(VERIFY_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String params = "secret=" + SECRET_KEY + "&response=" + token;
            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes());
                os.flush();
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            return response.toString().contains("\"success\":true");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public class RecaptchaCallback {
        private final CompletableFuture<String> tokenFuture;

        public RecaptchaCallback(CompletableFuture<String> tokenFuture) {
            this.tokenFuture = tokenFuture;
        }

        public void processToken(String token) {
            tokenFuture.complete(token);
        }
    }
}