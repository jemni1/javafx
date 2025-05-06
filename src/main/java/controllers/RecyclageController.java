package controllers;


import entities.CollecteDechet;
import entities.RecyclageDechet;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;

import services.PdfExportService;
import services.RecyclageService;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class RecyclageController {

    @FXML private TableView<RecyclageDechet> recyclageTable;
    @FXML private TableColumn<RecyclageDechet, Integer> idColumn;
    @FXML private TableColumn<RecyclageDechet, String> dateColumn;
    @FXML private TableColumn<RecyclageDechet, Double> quantiteRecycléeColumn;
    @FXML private TableColumn<RecyclageDechet, Double> energieProduiteColumn;
    @FXML private TableColumn<RecyclageDechet, String> utilisationColumn;
    @FXML private TableColumn<RecyclageDechet, String> dateRecyclageColumn;

    private final RecyclageService service;

    public RecyclageController() throws SQLException {
        this.service = RecyclageService.getInstance();
    }

    @FXML
    public void initialize() {


        dateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDateDebut().toString()));
        quantiteRecycléeColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getQuantiteRecyclage()).asObject());
        energieProduiteColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getEnergieProduite()).asObject());
        utilisationColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getUtilisation()));
        dateRecyclageColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDateFin().toString()));

        // Charger les recyclages
        try {
            ObservableList<RecyclageDechet> recyclages = FXCollections.observableList(service.getAllRecyclages());
            recyclageTable.setItems(recyclages);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur lors de la récupération des données.");
        }
    }

    @FXML
    private void ajouterRecyclage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRecyclageView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter Recyclage");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Rafraîchir la liste des recyclages après l'ajout
            recyclageTable.getItems().clear();
            recyclageTable.getItems().addAll(service.getAllRecyclages());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ajout du recyclage.");
        }
    }

    @FXML
    private void modifierRecyclage() {
        RecyclageDechet selectedRecyclage = recyclageTable.getSelectionModel().getSelectedItem();
        if (selectedRecyclage != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierRecyclageView.fxml"));
                Parent root = loader.load();
                ModifierRecyclageController controller = loader.getController();
                controller.setRecyclageDechet(selectedRecyclage);

                Stage stage = new Stage();
                stage.setTitle("Modifier Recyclage");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

                // Rafraîchir la liste des recyclages après modification
                recyclageTable.getItems().clear();
                recyclageTable.getItems().addAll(service.getAllRecyclages());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur lors de la modification du recyclage.");
            }
        } else {
            showAlert("Veuillez sélectionner un recyclage à modifier.");
        }
    }

    @FXML
    private void supprimerRecyclage() {
        RecyclageDechet selectedRecyclage = recyclageTable.getSelectionModel().getSelectedItem();
        if (selectedRecyclage != null) {
            // Créer une boîte de dialogue de confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmer la suppression");
            alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce recyclage ?");
            alert.setContentText("Cette action est irréversible.");

            // Attendre la réponse de l'utilisateur
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Supprimer le recyclage si l'utilisateur a confirmé
                    try {
                        service.supprimerRecyclageAvecCollectes(selectedRecyclage.getId());
                        recyclageTable.getItems().remove(selectedRecyclage);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert("Erreur lors de la suppression du recyclage.");
                    }
                }
            });
        } else {
            showAlert("Veuillez sélectionner un recyclage à supprimer.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.showAndWait();
    }

    @FXML
    public void afficherDetailsRecyclage(javafx.event.ActionEvent actionEvent) {
        RecyclageDechet selectedRecyclage = recyclageTable.getSelectionModel().getSelectedItem();
        if (selectedRecyclage != null) {
            StringBuilder detailsBuilder = new StringBuilder();

            detailsBuilder.append(String.format(
                    "Date début: %s\nQuantité recyclée: %.2f kg\nÉnergie produite: %.2f kWh\nUtilisation: %s\nDate fin: %s\n\n",
                    selectedRecyclage.getDateDebut(),
                    selectedRecyclage.getQuantiteRecyclage(),
                    selectedRecyclage.getEnergieProduite(),
                    selectedRecyclage.getUtilisation(),
                    selectedRecyclage.getDateFin()
            ));

            // Afficher les collectes associées
            if (selectedRecyclage.getCollectes() != null && !selectedRecyclage.getCollectes().isEmpty()) {
                detailsBuilder.append("Collectes associées :\n");
                for (CollecteDechet collecte : selectedRecyclage.getCollectes()) {
                    detailsBuilder.append(String.format(
                            "- Type de déchet: %s\n  Quantité: %.2f kg\n  Du: %s au %s\n\n",
                            collecte.getTypeDechet(),
                            collecte.getQuantite(),
                            collecte.getDateDebut(),
                            collecte.getDateFin()
                    ));
                }
            } else {
                detailsBuilder.append("Aucune collecte associée.\n");
            }

            String details = detailsBuilder.toString();

            // Affichage de l’image s’il y en a une
            if (selectedRecyclage.getImageUrl() != null && !selectedRecyclage.getImageUrl().isEmpty()) {
                ImageView imageView = new ImageView(new Image("file:" + selectedRecyclage.getImageUrl()));
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Détails du recyclage");
                alert.setHeaderText(null);
                alert.setContentText(details);
                alert.setGraphic(imageView);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); // pour agrandir si nécessaire
                alert.showAndWait();
            } else {
                // Afficher les détails sans image
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Détails du recyclage");
                alert.setHeaderText(null);
                alert.setContentText(details);
                alert.showAndWait();
            }
        } else {
            showAlert("Veuillez sélectionner un recyclage pour voir les détails.");
        }
    }



    @FXML
    private void exporterPDF() {
        RecyclageDechet selectedRecyclage = recyclageTable.getSelectionModel().getSelectedItem();
        if (selectedRecyclage != null) {
            try {
                PdfExportService pdfService = new PdfExportService();

                StringBuilder htmlContent = new StringBuilder();


// En-tête et styles CSS
                htmlContent.append("<!DOCTYPE html>")
                        .append("<html lang=\"fr\"><head>")
                        .append("<meta charset=\"UTF-8\">")
                        .append("<title>Rapport de Recyclage Agricole</title>")
                        .append("<style>")
                        .append(":root {")
                        .append("  --primary-color: #2e7d32;") // Vert foncé
                        .append("  --secondary-color: #558b2f;") // Vert moyen
                        .append("  --accent-color: #8bc34a;") // Vert clair
                        .append("  --light-bg: #f1f8e9;") // Fond vert pâle
                        .append("  --text-color: #33691e;") // Texte vert foncé
                        .append("  --border-color: #aed581;") // Bordure verte claire
                        .append("}")
                        .append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: var(--light-bg); color: #333; }")
                        .append(".container { max-width: 900px; margin: 0 auto; padding: 40px 20px; }")
                        .append(".header { background-color: var(--primary-color); color: white; padding: 30px; border-radius: 15px 15px 0 0; ")
                        .append("          position: relative; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }")
                        .append(".header::before { content: \"\"; position: absolute; top: 0; left: 0; right: 0; bottom: 0; ")
                        .append("                 background-image: url('data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\" viewBox=\"0 0 100 100\">")
                        .append("                 <path fill=\"%233c9f40\" opacity=\"0.1\" d=\"M10 30 C 20 5, 50 5, 60 30 S 100 55, 90 80 S 50 95, 40 70 S 0 45, 10 30 Z\"/></svg>'); ")
                        .append("                 background-size: 100px; opacity: 0.2; }")
                        .append(".header-content { position: relative; z-index: 1; display: flex; align-items: center; }")
                        .append(".logo { width: 80px; height: 80px; margin-right: 20px; background-color: white; border-radius: 50%; ")
                        .append("        display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 5px rgba(0,0,0,0.2); }")
                        .append(".title-area { flex-grow: 1; }")
                        .append(".title { font-size: 32px; font-weight: bold; margin: 0; }")
                        .append(".subtitle { font-size: 16px; margin-top: 5px; opacity: 0.9; }")
                        .append(".main-content { background-color: white; border-radius: 0 0 15px 15px; padding: 30px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }")
                        .append(".section { margin-bottom: 30px; border-bottom: 1px solid var(--border-color); padding-bottom: 20px; }")
                        .append(".section:last-child { border-bottom: none; margin-bottom: 0; padding-bottom: 0; }")
                        .append(".section-header { display: flex; align-items: center; margin-bottom: 20px; }")
                        .append(".section-icon { width: 36px; height: 36px; margin-right: 15px; color: var(--secondary-color); }")
                        .append("h2 { color: var(--primary-color); font-size: 24px; margin: 0; font-weight: 600; flex-grow: 1; }")
                        .append(".detail-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }")
                        .append(".detail-item { background-color: var(--light-bg); padding: 15px; border-radius: 10px; border-left: 4px solid var(--accent-color); }")
                        .append(".detail-label { font-weight: 600; color: var(--secondary-color); margin-bottom: 8px; font-size: 14px; text-transform: uppercase; }")
                        .append(".detail-value { font-size: 18px; font-weight: 500; }")
                        .append(".collecte-item { background-color: var(--light-bg); padding: 20px; border-radius: 10px; margin-bottom: 15px; ")
                        .append("                 border-left: 4px solid var(--accent-color); }")
                        .append(".collecte-item:last-child { margin-bottom: 0; }")
                        .append(".collecte-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }")
                        .append(".type-dechet { font-weight: 600; font-size: 18px; color: var(--primary-color); }")
                        .append(".quantite-badge { background-color: var(--secondary-color); color: white; padding: 5px 12px; border-radius: 20px; font-weight: 500; font-size: 14px; }")
                        .append(".collecte-dates { color: #666; font-size: 14px; }")
                        .append(".environmental-impact { background-color: var(--light-bg); border-radius: 10px; padding: 20px; margin-top: 30px; ")
                        .append("                        border: 1px dashed var(--border-color); }")
                        .append(".impact-header { text-align: center; color: var(--primary-color); margin-bottom: 15px; font-size: 18px; font-weight: 600; }")
                        .append(".impact-stats { display: flex; justify-content: space-around; text-align: center; }")
                        .append(".impact-stat { flex: 1; }")
                        .append(".impact-value { font-size: 24px; font-weight: 700; color: var(--secondary-color); margin-bottom: 5px; }")
                        .append(".impact-label { font-size: 14px; color: #666; }")
                        .append(".no-collecte { color: #999; font-style: italic; text-align: center; padding: 20px; }")
                        .append(".footer { text-align: center; color: #666; font-size: 14px; margin-top: 20px; }")
                        .append("@media (max-width: 768px) {")
                        .append("  .detail-grid { grid-template-columns: 1fr; }")
                        .append("  .impact-stats { flex-direction: column; gap: 15px; }")
                        .append("}")
                        .append("</style>")
                        .append("</head><body>");

// Structure principale
                htmlContent.append("<div class=\"container\">")

                        // En-tête avec logo SVG inline
                        .append("<div class=\"header\">")
                        .append("<div class=\"header-content\">")
                        .append("<div class=\"logo\">")
                        .append("<svg viewBox=\"0 0 24 24\" width=\"60\" height=\"60\">")
                        .append("<path fill=\"#2e7d32\" d=\"M12,3C16.97,3 21,7.03 21,12C21,16.97 16.97,21 12,21C7.03,21 3,16.97 3,12C3,7.03 7.03,3 12,3M12,5C8.14,5 5,8.14 5,12C5,15.86 8.14,19 12,19C15.86,19 19,15.86 19,12C19,8.14 15.86,5 12,5M16,15V13H8V15L12,18L16,15M16,8H13V10H16V12H13V15H11V12H8V10H11V8H8V6H16V8Z\"/>")
                        .append("</svg>")
                        .append("</div>")
                        .append("<div class=\"title-area\">")
                        .append("<h1 class=\"title\">Rapport de Recyclage Agricole</h1>")
                        .append("<div class=\"subtitle\">Transformation des déchets en ressources pour l'agriculture durable</div>")
                        .append("</div>")
                        .append("</div>")
                        .append("</div>");

// Contenu principal
                htmlContent.append("<div class=\"main-content\">");

// Section 1: Détails du recyclage
                htmlContent.append("<div class=\"section\">")
                        .append("<div class=\"section-header\">")
                        .append("<svg class=\"section-icon\" viewBox=\"0 0 24 24\">")
                        .append("<path fill=\"currentColor\" d=\"M21,16.5C21,16.88 20.79,17.21 20.47,17.38L12.57,21.82C12.41,21.94 12.21,22 12,22C11.79,22 11.59,21.94 11.43,21.82L3.53,17.38C3.21,17.21 3,16.88 3,16.5V7.5C3,7.12 3.21,6.79 3.53,6.62L11.43,2.18C11.59,2.06 11.79,2 12,2C12.21,2 12.41,2.06 12.57,2.18L20.47,6.62C20.79,6.79 21,7.12 21,7.5V16.5M12,4.15L6.04,7.5L12,10.85L17.96,7.5L12,4.15M5,15.91L11,19.29V12.58L5,9.21V15.91M19,15.91V9.21L13,12.58V19.29L19,15.91Z\"/>")
                        .append("</svg>")
                        .append("<h2>Détails du Recyclage</h2>")
                        .append("</div>")
                        .append("<div class=\"detail-grid\">");

// Informations sur le recyclage
                htmlContent.append("<div class=\"detail-item\">")
                        .append("<div class=\"detail-label\">Période de Recyclage</div>")
                        .append("<div class=\"detail-value\">Du ").append(selectedRecyclage.getDateDebut()).append(" au ").append(selectedRecyclage.getDateFin()).append("</div>")
                        .append("</div>");

                htmlContent.append("<div class=\"detail-item\">")
                        .append("<div class=\"detail-label\">Quantité Recyclée</div>")
                        .append("<div class=\"detail-value\">").append(selectedRecyclage.getQuantiteRecyclage()).append(" kg</div>")
                        .append("</div>");

                htmlContent.append("<div class=\"detail-item\">")
                        .append("<div class=\"detail-label\">Énergie Produite</div>")
                        .append("<div class=\"detail-value\">").append(selectedRecyclage.getEnergieProduite()).append(" kWh</div>")
                        .append("</div>");

                htmlContent.append("<div class=\"detail-item\">")
                        .append("<div class=\"detail-label\">Utilisation</div>")
                        .append("<div class=\"detail-value\">").append(selectedRecyclage.getUtilisation()).append("</div>")
                        .append("</div>");

                htmlContent.append("</div>") // Fermeture de detail-grid
                        .append("</div>"); // Fermeture de la section

// Section 2: Collectes associées
                htmlContent.append("<div class=\"section\">")
                        .append("<div class=\"section-header\">")
                        .append("<svg class=\"section-icon\" viewBox=\"0 0 24 24\">")
                        .append("<path fill=\"currentColor\" d=\"M18,10H6V5H18M12,17A2,2 0 0,1 10,15A2,2 0 0,1 12,13A2,2 0 0,1 14,15A2,2 0 0,1 12,17M13.5,12L21.1,3.5L19.7,2.1L13.5,9L12.1,7.6L10.7,9L13.5,12M3,10H1V18A2,2 0 0,0 3,20H18V18H3V10Z\"/>")
                        .append("</svg>")
                        .append("<h2>Collectes Associées</h2>")
                        .append("</div>");

// Liste des collectes
                if (selectedRecyclage.getCollectes() != null && !selectedRecyclage.getCollectes().isEmpty()) {
                    for (CollecteDechet collecte : selectedRecyclage.getCollectes()) {
                        htmlContent.append("<div class=\"collecte-item\">")
                                .append("<div class=\"collecte-header\">")
                                .append("<div class=\"type-dechet\">").append(collecte.getTypeDechet()).append("</div>")
                                .append("<div class=\"quantite-badge\">").append(collecte.getQuantite()).append(" kg</div>")
                                .append("</div>")
                                .append("<div class=\"collecte-dates\">Période: du ").append(collecte.getDateDebut()).append(" au ").append(collecte.getDateFin()).append("</div>")
                                .append("</div>");
                    }
                } else {
                    htmlContent.append("<div class=\"no-collecte\">Aucune collecte associée à ce recyclage.</div>");
                }

                htmlContent.append("</div>"); // Fermeture de la section


// Pied de page
                htmlContent.append("<div class=\"footer\">")
                        .append("Rapport généré le ").append(java.time.LocalDate.now().toString()).append(" • Agriculture Durable et Recyclage")
                        .append("</div>");

// Fermeture des balises
                htmlContent.append("</div>") // Fermeture de main-content
                        .append("</div>") // Fermeture de container
                        .append("</body></html>");


                // 1. Générer le PDF
                String pdfUrl = pdfService.exportToPDF(String.valueOf(htmlContent));

                // 2. Ouvrir un FileChooser pour choisir où sauvegarder le fichier PDF
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Enregistrer le PDF");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
                fileChooser.setInitialFileName("recyclage.pdf");

                Stage stage = (Stage) recyclageTable.getScene().getWindow(); // Récupérer la fenêtre actuelle
                File selectedFile = fileChooser.showSaveDialog(stage);

                if (selectedFile != null) {
                    // 3. Télécharger le PDF à cet emplacement
                    pdfService.downloadPdfToPath(pdfUrl, selectedFile);
                    showAlert("PDF exporté avec succès !");
                }

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur lors de l'export du PDF.");
            }
        } else {
            showAlert("Veuillez sélectionner un recyclage pour exporter en PDF.");
        }
    }



    @FXML
    private void afficherStatistique() {
        RecyclageDechet selectionne = recyclageTable.getSelectionModel().getSelectedItem();
        if (selectionne != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/StatistiquesView.fxml"));
                Parent root = loader.load();

                StatistiqueController controller = loader.getController();
                controller.setRecyclage(selectionne);

                Stage stage = new Stage();
                stage.setTitle("Statistiques du Recyclage");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un recyclage pour afficher ses statistiques.");
            alert.showAndWait();
        }
    }


}
