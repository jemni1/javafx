package edu.connection.controllers;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import edu.connection.entities.Commande;
import edu.connection.services.CommandeService;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class commande {

    @FXML
    private ListView<Commande> listCommandes;

    @FXML
    private Label produitCommandes;

    @FXML
    private TextField searchField;

    private final CommandeService commandeService = new CommandeService();

    @FXML
    public void initialize() {
        afficherCommandes();

        // Event listener sur le TextField pour filtrer les commandes
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCommandes(newValue);
        });

        produitCommandes.setOnMouseClicked(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/userProduit.fxml"));
                Parent root = loader.load();
                produitCommandes.getScene().setRoot(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Configuration du cell factory pour personnaliser l'affichage des items
        listCommandes.setCellFactory(param -> new ListCell<Commande>() {
            @Override
            protected void updateItem(Commande commande, boolean empty) {
                super.updateItem(commande, empty);

                if (empty || commande == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Création du layout pour chaque item
                    HBox itemBox = new HBox(20);
                    itemBox.setStyle("-fx-padding: 15px; -fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

                    // Partie gauche - Informations de la commande
                    VBox infoBox = new VBox(5);

                    Label nomLabel = new Label(commande.getNom());
                    nomLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                    Label detailsLabel = new Label(String.format(
                            "Prix: %d DT  |  Quantité: %d  |  Date: %s",
                            commande.getPrix(),
                            commande.getQuantite(),
                            new SimpleDateFormat("dd/MM/yyyy HH:mm").format(commande.getDate())
                    ));
                    detailsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

                    infoBox.getChildren().addAll(nomLabel, detailsLabel);

                    // Partie droite - Boutons d'action
                    HBox buttonBox = new HBox(10);
                    buttonBox.setAlignment(Pos.CENTER_RIGHT);

                    Button btnVoir = new Button("👁 Voir");
                    Button btnSupprimer = new Button("🗑 Supprimer");
                    Button btnPdf = new Button("📄 PDF");

                    // Styles des boutons
                    btnVoir.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white;");
                    btnSupprimer.setStyle("-fx-background-color: #e53935; -fx-text-fill: white;");
                    btnPdf.setStyle("-fx-background-color: #43a047; -fx-text-fill: white;");

                    // Gestion des événements (identique à votre code original)
                    btnVoir.setOnAction(event -> voirDetailsCommande(commande));
                    btnSupprimer.setOnAction(event -> supprimerCommande(commande));
                    btnPdf.setOnAction(event -> genererPDF(commande));

                    buttonBox.getChildren().addAll(btnVoir, btnSupprimer, btnPdf);

                    // Configuration de la largeur
                    HBox.setHgrow(infoBox, Priority.ALWAYS);
                    itemBox.getChildren().addAll(infoBox, buttonBox);

                    setGraphic(itemBox);
                }
            }
        });
    }

    private void afficherCommandes() {
        List<Commande> commandes = commandeService.getList();
        listCommandes.getItems().setAll(commandes);
    }

    private void filterCommandes(String filterText) {
        List<Commande> commandes = commandeService.getList();

        if (filterText == null || filterText.isEmpty()) {
            listCommandes.getItems().setAll(commandes);
        } else {
            List<Commande> filteredCommandes = commandes.stream()
                    .filter(commande -> commande.getNom().toLowerCase().contains(filterText.toLowerCase()))
                    .collect(Collectors.toList());
            listCommandes.getItems().setAll(filteredCommandes);
        }
    }

    // Méthodes pour les actions (extrait de votre code original)
    private void voirDetailsCommande(Commande commande) {
        try {
            // Configuration de la fenêtre
            Stage detailsStage = new Stage();
            detailsStage.setTitle("Détails de la commande #" + commande.getId());
            detailsStage.initModality(Modality.APPLICATION_MODAL);

            // Layout principal avec gradient et ombre
            BorderPane mainLayout = new BorderPane();

            // En-tête avec bannière
            StackPane header = new StackPane();
            header.setStyle("-fx-background-color: linear-gradient(to right, #006400, #008000); -fx-padding: 20px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3);");

            VBox headerContent = new VBox(5);
            headerContent.setAlignment(Pos.CENTER);

            Label titleLabel = new Label("Détails de la commande");
            titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

            Label idLabel = new Label("Commande #" + commande.getId());
            idLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-opacity: 0.9;");

            headerContent.getChildren().addAll(titleLabel, idLabel);
            header.getChildren().add(headerContent);

            // Contenu central
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

            VBox contentBox = new VBox(15);
            contentBox.setStyle("-fx-background-color: white; -fx-padding: 25px;");
            contentBox.setAlignment(Pos.TOP_LEFT);

            // Section des informations
            VBox infoSection = new VBox(12);
            infoSection.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20px; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

            Label infoTitle = new Label("Informations générales");
            infoTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");

            // Grille d'informations
            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(15);
            infoGrid.setVgap(10);

            // Styles pour les labels
            String labelStyle = "-fx-font-size: 14px; -fx-text-fill: #555555;";
            String valueStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;";

            // Ligne 1: Produit
            Label produitLabel = new Label("Produit:");
            produitLabel.setStyle(labelStyle);
            Label produitValue = new Label(commande.getNom());
            produitValue.setStyle(valueStyle);
            infoGrid.add(produitLabel, 0, 0);
            infoGrid.add(produitValue, 1, 0);

            // Ligne 2: Prix
            Label prixLabel = new Label("Prix:");
            prixLabel.setStyle(labelStyle);
            Label prixValue = new Label(String.format("%.2f DT", (double)commande.getPrix()));
            prixValue.setStyle(valueStyle + "-fx-text-fill: #006400;");
            infoGrid.add(prixLabel, 0, 1);
            infoGrid.add(prixValue, 1, 1);

            // Ligne 3: Quantité
            Label quantiteLabel = new Label("Quantité:");
            quantiteLabel.setStyle(labelStyle);
            Label quantiteValue = new Label(String.valueOf(commande.getQuantite()));
            quantiteValue.setStyle(valueStyle);
            infoGrid.add(quantiteLabel, 0, 2);
            infoGrid.add(quantiteValue, 1, 2);

            // Ligne 4: Date
            Label dateLabel = new Label("Date:");
            dateLabel.setStyle(labelStyle);
            Label dateValue = new Label(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(commande.getDate()));
            dateValue.setStyle(valueStyle);
            infoGrid.add(dateLabel, 0, 3);
            infoGrid.add(dateValue, 1, 3);

            // Ligne 5: Total
            Label totalLabel = new Label("Total:");
            totalLabel.setStyle(labelStyle);
            Label totalValue = new Label(String.format("%.2f DT", (double)commande.getPrix() * commande.getQuantite()));
            totalValue.setStyle(valueStyle + "-fx-font-size: 16px; -fx-text-fill: #006400;");
            infoGrid.add(totalLabel, 0, 4);
            infoGrid.add(totalValue, 1, 4);

            infoSection.getChildren().addAll(infoTitle, infoGrid);

            // Section QR Code
            VBox qrSection = new VBox(15);
            qrSection.setAlignment(Pos.CENTER);
            qrSection.setStyle("-fx-padding: 20px; -fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 8px;");

            Label qrLabel = new Label("QR Code de la commande");
            qrLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");

            // Génération du QR Code
            String qrContent = String.format(
                    "ID: %d\nProduit: %s\nPrix: %.2f DT\nQuantité: %d\nDate: %s\nTotal: %.2f DT",
                    commande.getId(),
                    commande.getNom(),
                    (double)commande.getPrix(),
                    commande.getQuantite(),
                    new SimpleDateFormat("dd/MM/yyyy HH:mm").format(commande.getDate()),
                    (double)commande.getPrix() * commande.getQuantite()
            );

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            WritableImage writableImage = new WritableImage(width, height);
            PixelWriter pixelWriter = writableImage.getPixelWriter();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pixelWriter.setColor(x, y, bitMatrix.get(x, y) ? javafx.scene.paint.Color.BLACK : javafx.scene.paint.Color.WHITE);
                }
            }

            ImageView qrImageView = new ImageView(writableImage);
            qrImageView.setFitWidth(180);
            qrImageView.setFitHeight(180);
            qrImageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 2);");

            Label scanLabel = new Label("Scannez pour voir les détails");
            scanLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #777777; -fx-font-style: italic;");

            qrSection.getChildren().addAll(qrLabel, qrImageView, scanLabel);

            // Assemblage des sections
            contentBox.getChildren().addAll(infoSection, qrSection);
            scrollPane.setContent(contentBox);

            // Pied de page avec boutons
            HBox footer = new HBox(15);
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.setStyle("-fx-padding: 15px 25px; -fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");

            Button printButton = new Button("Imprimer");
            printButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 5px;");
            printButton.setOnAction(e -> {
                // Logique d'impression
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Impression");
                alert.setHeaderText("Impression lancée");
                alert.setContentText("La commande #" + commande.getId() + " est en cours d'impression.");
                alert.showAndWait();
            });

            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 5px;");
            closeButton.setOnAction(e -> detailsStage.close());

            footer.getChildren().addAll(printButton, closeButton);

            // Assemblage final
            mainLayout.setTop(header);
            mainLayout.setCenter(scrollPane);
            mainLayout.setBottom(footer);

            Scene scene = new Scene(mainLayout, 450, 700);
            detailsStage.setScene(scene);
            detailsStage.show();

        } catch (WriterException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de la génération du QR code");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'affichage des détails");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void supprimerCommande(Commande commande) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer cette commande ?");
        confirmation.setContentText("Commande de : " + commande.getNom());

        ButtonType boutonOui = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType boutonNon = new ButtonType("Non", ButtonBar.ButtonData.NO);

        confirmation.getButtonTypes().setAll(boutonOui, boutonNon);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == boutonOui) {
                commandeService.deleteEntity(commande);
                afficherCommandes();
            }
        });
    }

    private void genererPDF(Commande commande) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier PDF");
            fileChooser.setInitialFileName("commande_" + commande.getId() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));
            File file = fileChooser.showSaveDialog(listCommandes.getScene().getWindow());

            if (file != null) {
                String logoBase64 = "";
                try {
                    InputStream inputStream = getClass().getResourceAsStream("/logo.png");
                    if (inputStream != null) {
                        byte[] imageBytes = inputStream.readAllBytes();
                        inputStream.close();
                        logoBase64 = Base64.getEncoder().encodeToString(imageBytes);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement du logo: " + e.getMessage());
                    e.printStackTrace();
                }

                String htmlContent = String.format(
                        "<!DOCTYPE html>\n" +
                                "<html lang=\"fr\">\n" +
                                "<head>\n" +
                                "    <meta charset=\"UTF-8\"/>\n" +
                                "    <style>\n" +
                                "        body {\n" +
                                "            font-family: Arial, sans-serif;\n" +
                                "            margin: 30px;\n" +
                                "            color: #333;\n" +
                                "        }\n" +
                                "        .container {\n" +
                                "            max-width: 600px;\n" +
                                "            border: 2px solid #4CAF50;\n" +
                                "            padding: 20px;\n" +
                                "            border-radius: 10px;\n" +
                                "            margin: 0 auto;\n" +
                                "        }\n" +
                                "        .logo-header {\n" +
                                "            text-align: center;\n" +
                                "            margin-bottom: 20px;\n" +
                                "            background-color: #e8f5e9;\n" +
                                "            padding: 15px;\n" +
                                "            border-radius: 8px;\n" +
                                "        }\n" +
                                "        .logo-img {\n" +
                                "            max-width: 200px;\n" +
                                "            max-height: 100px;\n" +
                                "            margin-bottom: 10px;\n" +
                                "        }\n" +
                                "        .invoice-number {\n" +
                                "            font-size: 22px;\n" +
                                "            font-weight: bold;\n" +
                                "            color: #2E7D32;\n" +
                                "        }\n" +
                                "        .invoice-header h1 {\n" +
                                "            text-align: center;\n" +
                                "            color: #2E7D32;\n" +
                                "            border-bottom: 2px solid #4CAF50;\n" +
                                "            padding-bottom: 10px;\n" +
                                "        }\n" +
                                "        .invoice-info {\n" +
                                "            margin-top: 20px;\n" +
                                "            width: 100%%;\n" +
                                "            border-collapse: collapse;\n" +
                                "        }\n" +
                                "        .invoice-info td {\n" +
                                "            padding: 8px;\n" +
                                "            vertical-align: top;\n" +
                                "        }\n" +
                                "        .info-left {\n" +
                                "            width: 50%%;\n" +
                                "        }\n" +
                                "        .info-right {\n" +
                                "            width: 50%%;\n" +
                                "            text-align: right;\n" +
                                "        }\n" +
                                "        .info-group p {\n" +
                                "            margin: 8px 0;\n" +
                                "        }\n" +
                                "        .info-group p strong {\n" +
                                "            color: #2E7D32;\n" +
                                "        }\n" +
                                "        .total {\n" +
                                "            text-align: right;\n" +
                                "            margin-top: 30px;\n" +
                                "            font-size: 20px;\n" +
                                "            font-weight: bold;\n" +
                                "            color: #2E7D32;\n" +
                                "            background-color: #e8f5e9;\n" +
                                "            padding: 10px;\n" +
                                "            border-radius: 5px;\n" +
                                "        }\n" +
                                "        .footer {\n" +
                                "            text-align: center;\n" +
                                "            margin-top: 40px;\n" +
                                "            font-style: italic;\n" +
                                "            font-size: 14px;\n" +
                                "            background-color: #e8f5e9;\n" +
                                "            padding: 10px;\n" +
                                "            border-radius: 5px;\n" +
                                "            color: #2E7D32;\n" +
                                "        }\n" +
                                "        .footer a {\n" +
                                "            color: #2E7D32;\n" +
                                "            text-decoration: none;\n" +
                                "            font-weight: bold;\n" +
                                "        }\n" +
                                "    </style>\n" +
                                "</head>\n" +
                                "<body>\n" +
                                "    <div class=\"container\">\n" +
                                "        <div class=\"logo-header\">\n" +
                                "            %s\n" +
                                "            <div class=\"invoice-number\">Facture #%d</div>\n" +
                                "        </div>\n" +
                                "        <div class=\"invoice-header\">\n" +
                                "            <h1>Facture</h1>\n" +
                                "        </div>\n" +
                                "        <table class=\"invoice-info\">\n" +
                                "            <tr>\n" +
                                "                <td class=\"info-left\">\n" +
                                "                    <div class=\"info-group\">\n" +
                                "                        <p><strong>Produit:</strong> %s</p>\n" +
                                "                        <p><strong>Quantité:</strong> %d</p>\n" +
                                "                    </div>\n" +
                                "                </td>\n" +
                                "                <td class=\"info-right\">\n" +
                                "                    <div class=\"info-group\">\n" +
                                "                        <p><strong>Prix unitaire:</strong> %d €</p>\n" +
                                "                        <p><strong>Date:</strong> %s</p>\n" +
                                "                    </div>\n" +
                                "                </td>\n" +
                                "            </tr>\n" +
                                "        </table>\n" +
                                "        <div class=\"total\">\n" +
                                "            <strong>Total:</strong> %d €\n" +
                                "        </div>\n" +
                                "        <div class=\"footer\">\n" +
                                "            <p>🌿 Merci de votre confiance ! 🌿</p>\n" +
                                "            <p>DataFarm - Votre partenaire de confiance</p>\n" +
                                "            <p><a href=\"https://www.datafarm.com\">www.datafarm.com</a></p>\n" +
                                "        </div>\n" +
                                "    </div>\n" +
                                "</body>\n" +
                                "</html>",
                        logoBase64.isEmpty() ? "" : "<img class=\"logo-img\" src=\"data:image/jpeg;base64," + logoBase64 + "\" alt=\"Logo DataFarm\"/>",
                        commande.getId(),
                        commande.getNom(),
                        commande.getQuantite(),
                        commande.getPrix() / commande.getQuantite(),
                        new SimpleDateFormat("dd-MM-yyyy HH:mm").format(commande.getDate()),
                        commande.getPrix()
                );

                try (OutputStream os = new FileOutputStream(file)) {
                    PdfRendererBuilder builder = new PdfRendererBuilder();
                    builder.useFastMode();
                    builder.withHtmlContent(htmlContent, null);
                    builder.toStream(os);
                    builder.run();
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("PDF généré");
                alert.setHeaderText(null);
                alert.setContentText("Le fichier PDF a été généré avec succès :\n" + file.getAbsolutePath());
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de la génération du PDF");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}