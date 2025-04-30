package services;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.model.*;
import java.io.File;
import java.nio.file.Files;

public class DocuSignService {
    private EnvelopesApi envelopesApi;

    public DocuSignService() throws Exception {
        ApiClient apiClient = DocuSignApiClient.getApiClient();
        this.envelopesApi = new EnvelopesApi(apiClient);
    }

    public EnvelopeSummary sendDocumentForSignature(String filePath) throws Exception {
        File file = new File(filePath);
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        // Préparer les documents à envoyer
        Document document = new Document();
        document.setDocumentBase64(java.util.Base64.getEncoder().encodeToString(fileBytes));
        document.setName("RecyclagePDF"); // Nom du document
        document.setFileExtension("pdf");
        document.setDocumentId("1");

        // Préparer la page de signature
        Signer signer = new Signer();
        signer.setEmail("rayenghrairi53@gmail.com");
        signer.setName("Rayen Ghrairi");
        signer.setRecipientId("1");
        signer.setRoutingOrder("1");

        // Placer la signature sur le document
        SignHere signHere = new SignHere();
        signHere.setAnchorString("/sig/"); // Marque d'ancrage pour la signature
        signHere.setAnchorXOffset("0");
        signHere.setAnchorYOffset("0");

        // Créer la section de signataire
        signer.setTabs(new Tabs());
        signer.getTabs().setSignHereTabs(java.util.Arrays.asList(signHere));

        // Créer l'enveloppe
        EnvelopeDefinition envelopeDefinition = new EnvelopeDefinition();
        envelopeDefinition.setEmailSubject("Veuillez signer le recyclage PDF");
        envelopeDefinition.setDocuments(java.util.Arrays.asList(document));
        envelopeDefinition.setRecipients(new Recipients());
        envelopeDefinition.getRecipients().setSigners(java.util.Arrays.asList(signer));
        envelopeDefinition.setStatus("sent");

        // Envoyer l'enveloppe pour la signature
        EnvelopeSummary envelopeSummary = envelopesApi.createEnvelope("your_account_id", envelopeDefinition);
        return envelopeSummary;
    }
}
