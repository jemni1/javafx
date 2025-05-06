package services;

import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.Configuration;
import com.docusign.esign.client.auth.OAuth;
import java.io.FileInputStream;
import java.security.PrivateKey;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.List;

public class DocuSignApiClient {
    private static final String API_BASE_PATH = "https://demo.docusign.net/restapi"; // Point de terminaison de l'API DocuSign
    private static final String CLIENT_ID = "557719746fca0dfe5cb19ae3eb3c0ced";
    private static final String CLIENT_SECRET = "3199431e543222fc0a7aef781786d843";
    private static final String REDIRECT_URI = "your_redirect_uri"; // URI de redirection après autorisation
    private static final String IMPERSONATED_USER_ID = "your_impersonated_user_id"; // L'ID de l'utilisateur à imiter
    private static final String PRIVATE_KEY_PATH = "path/to/your/private_key.pem"; // Chemin vers la clé privée RSA

    public static ApiClient getApiClient() throws Exception {
        ApiClient apiClient = new ApiClient(API_BASE_PATH);
        apiClient.setOAuthBasePath("account-d.docusign.com");

        // Charger la clé privée RSA depuis un fichier
        PrivateKey privateKey = loadPrivateKey(PRIVATE_KEY_PATH);

        // Définir les scopes pour l'accès aux ressources
        List<String> scopes = Arrays.asList("signature", "impersonation");

        // Authentification avec JWT
        OAuth.OAuthToken oauthToken = apiClient.requestJWTUserToken(
                CLIENT_ID,
                IMPERSONATED_USER_ID,
                scopes,
                privateKey.getEncoded(),
                3600 // Durée d'expiration du token en secondes
        );

        // Définir le jeton d'accès pour l'API client
        apiClient.setAccessToken(oauthToken.getAccessToken(), oauthToken.getExpiresIn());

        // Configurer l'API client avec les informations d'authentification
        Configuration.setDefaultApiClient(apiClient);
        return apiClient;
    }

    private static PrivateKey loadPrivateKey(String privateKeyPath) throws Exception {
        FileInputStream fis = new FileInputStream(privateKeyPath);
        byte[] keyBytes = fis.readAllBytes();
        fis.close();

        // Charger la clé privée RSA à partir des bytes
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(keyBytes));
    }
}
