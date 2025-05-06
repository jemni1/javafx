package services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

public class PdfExportService {

    private static final String API_KEY = "rayenghrairi53@gmail.com_RnXsfz7CcoQHdM1DkKabwXdCS9BPrVzqBVuDZW2WgCNjhAgsCxF5yrkbuLYX15f8"; // Remplace par ta clé API PDF.co

    // Méthode pour exporter en PDF à partir du contenu HTML
    public String exportToPDF(String htmlContent) throws Exception {
        // Corps de la requête en JSON avec le paramètre html
        String bodyJson = "{"
                + "\"name\": \"recyclage_details.pdf\","
                + "\"html\": \"" + htmlContent.replace("\"", "\\\"") + "\""
                + "}";

        // Création de la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.pdf.co/v1/pdf/convert/from/html"))
                .header("Content-Type", "application/json")
                .header("x-api-key", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))  // Corps de la requête
                .build();

        // Envoi de la requête et réception de la réponse
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Vérification du statut de la réponse
        if (response.statusCode() == 200) {
            System.out.println("PDF exporté avec succès !");

            // Parser la réponse JSON
            JSONObject jsonResponse = new JSONObject(response.body());
            String pdfDownloadLink = jsonResponse.getString("url");  // Récupérer l'URL du PDF

            

            return pdfDownloadLink;  // Renvoie l'URL du PDF
        } else {
            System.out.println("Erreur lors de l'export du PDF: " + response.body());
            throw new Exception("Erreur lors de la génération du PDF");
        }
    }


    // Nouvelle méthode qui télécharge vers un chemin précis
    public void downloadPdfToPath(String pdfUrl, File destinationFile) throws IOException {
        URL url = new URL(pdfUrl);
        InputStream inputStream = url.openStream();

        try (FileOutputStream fileOutputStream = new FileOutputStream(destinationFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
        } finally {
            inputStream.close();
        }
    }
}
