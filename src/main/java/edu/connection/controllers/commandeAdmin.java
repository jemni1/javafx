package edu.connection.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import edu.connection.entities.Commande;
import edu.connection.services.CommandeService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.List;

public class commandeAdmin {

    @FXML
    private TableView<Commande> commandesTable;

    @FXML
    private TableColumn<Commande, Integer> idClientColumn;

    @FXML
    private TableColumn<Commande, Integer> idProduitColumn;

    @FXML
    private TableColumn<Commande, Integer> quantiteColumn;

    @FXML
    private TableColumn<Commande, String> dateColumn;

    @FXML
    private TableColumn<Commande, Integer> prixTotalColumn;

    @FXML
    private TableColumn<Commande, String> nomColumn;

    @FXML
    private TableColumn<Commande, Void> actionsColumn;

    @FXML
    private TextField searchField;

    private final CommandeService commandeService = new CommandeService();

    @FXML
    public void initialize() {

        afficherCommandes();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> onSearchChanged());

    }
    @FXML
    private void goToListeProduits(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/listeProduits.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText("Impossible de charger la page des produits");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    private void goToStatistique(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/statistiques.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText("Impossible de charger la page des commandes");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    private void goToObjectDetection(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ObjectDetectionView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText("Impossible de charger la page de detection");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onSearchChanged() {
        String searchText = searchField.getText().trim();
        List<Commande> commandes;

        if (searchText.isEmpty()) {
            commandes = commandeService.getList();  // Récupère toutes les commandes si le champ est vide
        } else {
            commandes = commandeService.searchByName(searchText);  // Recherche les commandes par nom
        }

        commandesTable.getItems().setAll(commandes);  // Met à jour la TableView avec les résultats de recherche
    }

    private void afficherCommandes() {
        List<Commande> commandes = commandeService.getList();

        // Mapping des colonnes
        idClientColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getIdClient()).asObject());

        idProduitColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getIdProduit()).asObject());

        quantiteColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getQuantite()).asObject());

        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(cellData.getValue().getDate())));

        prixTotalColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getPrix()).asObject());

        nomColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNom()));

        ajouterColonneActions();

        commandesTable.getItems().setAll(commandes);
    }

    private void ajouterColonneActions() {
        actionsColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Commande, Void> call(final TableColumn<Commande, Void> param) {
                return new TableCell<>() {
                    private final Button btnSupprimer = new Button("🗑 Supprimer");
                    private final Button btnVoir = new Button("👁 Voir");
                    private final Button btnPdf = new Button("📄 PDF");

                    {

                        btnSupprimer.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                        btnVoir.setStyle("-fx-background-color: #1e88e5; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                        btnPdf.setStyle("-fx-background-color: #43a047; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                        btnPdf.setOnAction((ActionEvent event) -> {
                            Commande commande = getTableView().getItems().get(getIndex());

                            try {
                                FileChooser fileChooser = new FileChooser();
                                fileChooser.setTitle("Enregistrer le fichier PDF");
                                fileChooser.setInitialFileName("commande_" + commande.getId() + ".pdf");
                                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));
                                File file = fileChooser.showSaveDialog(btnPdf.getScene().getWindow());

                                if (file != null) {
                                    // Charger et encoder l'image du logo en base64
                                    String logoBase64 = "";
                                    try {
                                        // Lire le fichier image depuis les ressources
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

                                    // Préparer le contenu HTML avec le logo
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
                        });

                        btnSupprimer.setOnAction(event -> {
                            Commande commande = getTableView().getItems().get(getIndex());

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
                        });


                        btnVoir.setOnAction((ActionEvent event) -> {
                            Commande commande = getTableView().getItems().get(getIndex());

                            try {
                                Stage detailsStage = new Stage();
                                detailsStage.setTitle("Détails Complet de la Commande");

                                // Conteneur principal - taille augmentée
                                VBox layout = new VBox(25);
                                layout.setStyle("-fx-padding: 35; -fx-background-color: #f8fff8; -fx-border-color: #c8e6c9; "
                                        + "-fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12;");
                                layout.setAlignment(Pos.TOP_CENTER);
                                layout.setPrefSize(500, 650);
                                layout.setEffect(new DropShadow(15, Color.rgb(0, 100, 0, 0.15)));

                                // En-tête plus visible
                                Label titleLabel = new Label("📋 COMMANDE #" + commande.getId());
                                titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2e7d32; "
                                        + "-fx-padding: 0 0 15 0;");

                                // Section informations - plus large
                                VBox infoBox = new VBox(15);
                                infoBox.setAlignment(Pos.TOP_LEFT);
                                infoBox.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-background-radius: 10; "
                                        + "-fx-border-color: #e0f0e0; -fx-border-radius: 10; -fx-border-width: 1; "
                                        + "-fx-pref-width: 400;");

                                // Labels d'information avec style amélioré
                                String labelStyle = "-fx-font-size: 15px; -fx-text-fill: #2d6a4f; -fx-padding: 3 0;";

                                Label nomLabel = new Label("▪ Produit: " + commande.getNom());
                                nomLabel.setStyle(labelStyle + "-fx-font-weight: bold;");

                                Label prixLabel = new Label("▪ Prix total: " + commande.getPrix() + " DT");
                                prixLabel.setStyle(labelStyle);

                                Label quantiteLabel = new Label("▪ Quantité: " + commande.getQuantite());
                                quantiteLabel.setStyle(labelStyle);

                                Label dateLabel = new Label("▪ Date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(commande.getDate()));
                                dateLabel.setStyle(labelStyle);

                                // Séparateur visuel
                                Separator separator = new Separator();
                                separator.setStyle("-fx-padding: 10 0;");

                                infoBox.getChildren().addAll(nomLabel, prixLabel, quantiteLabel, dateLabel, separator);

                                // Section QR Code plus grande
                                VBox qrBox = new VBox(15);
                                qrBox.setAlignment(Pos.CENTER);
                                qrBox.setStyle("-fx-padding: 20 0;");

                                Label qrTitle = new Label("CODE QR DE LA COMMANDE");
                                qrTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

                                try {
                                    String qrContent = String.format(
                                            "ID Commande: %d\nProduit: %s\nPrix Total: %d DT\nQuantité: %d\nDate: %s",
                                            commande.getId(),
                                            commande.getNom(),
                                            commande.getPrix(),
                                            commande.getQuantite(),
                                            new SimpleDateFormat("dd/MM/yyyy HH:mm").format(commande.getDate())
                                    );

                                    QRCodeWriter qrCodeWriter = new QRCodeWriter();
                                    BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 250, 250);

                                    WritableImage writableImage = new WritableImage(bitMatrix.getWidth(), bitMatrix.getHeight());
                                    PixelWriter pixelWriter = writableImage.getPixelWriter();

                                    for (int x = 0; x < bitMatrix.getWidth(); x++) {
                                        for (int y = 0; y < bitMatrix.getHeight(); y++) {
                                            pixelWriter.setColor(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                                        }
                                    }

                                    ImageView qrImageView = new ImageView(writableImage);
                                    qrImageView.setFitWidth(200);  // QR code plus grand
                                    qrImageView.setFitHeight(200);
                                    qrImageView.setStyle("-fx-border-color: #c8e6c9; -fx-border-width: 1; -fx-border-radius: 5;");

                                    qrBox.getChildren().addAll(qrTitle, qrImageView);
                                } catch (WriterException e) {
                                    Label errorLabel = new Label("Erreur de génération du QR Code");
                                    errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                                    qrBox.getChildren().add(errorLabel);
                                }

                                // Bouton de fermeture amélioré
                                Button closeButton = new Button("Fermer");
                                closeButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; "
                                        + "-fx-background-radius: 5; -fx-padding: 10 30; -fx-font-size: 14px;");
                                closeButton.setOnAction(e -> detailsStage.close());

                                // Assemblage des composants
                                layout.getChildren().addAll(titleLabel, infoBox, qrBox, closeButton);

                                // Configuration de la scène
                                Scene scene = new Scene(layout);
                                detailsStage.setScene(scene);
                                detailsStage.show();

                            } catch (Exception e) {
                                e.printStackTrace();
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Erreur");
                                alert.setHeaderText("Erreur lors de l'affichage des détails");
                                alert.setContentText(e.getMessage());
                                alert.showAndWait();
                            }
                        });}

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox hBox = new HBox(15, btnVoir, btnSupprimer, btnPdf);
                            setGraphic(hBox);
                        }
                    }
                };
            }
        });
    }
}
