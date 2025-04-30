package controllers;

import entities.CollecteDechet;
import entities.RecyclageDechet;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import services.MailUtil;

import services.RecyclageService;
import services.CollecteService;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AjouterRecyclageController {

    @FXML private TextField quantiteRecyclageField;
    @FXML private TextField energieProduiteField;
    @FXML private ComboBox<String> utilisationEnergieComboBox;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private Button imageButton;
    @FXML private ImageView imageView;  // Assure-toi que l'ImageView est bien initialisé via FXML
    @FXML private Label messageLabel;
    @FXML private TextField searchField;
    @FXML private ListView<CollecteDechet> collecteListView;

    private String imagePath = ""; // Variable pour stocker le chemin de l'image
    private final RecyclageService service = RecyclageService.getInstance();
    private final CollecteService collecteService = CollecteService.getInstance();  // Nouveau service pour gérer les collectes

    public AjouterRecyclageController() throws SQLException {
    }

    @FXML
    private void initialize() {
        utilisationEnergieComboBox.getItems().addAll(
                "Irrigation", "Chauffage", "Alimentation", "Stockage d'énergie", "Autre"
        );

        // Charger toutes les collectes
        List<CollecteDechet> collectes = collecteService.getCollectesSansRecyclage();
        collecteListView.getItems().addAll(collectes);

        // Affichage personnalisé de chaque collecte et gestion de la sélection
        collecteListView.setCellFactory(param -> new ListCell<CollecteDechet>() {
            @Override
            protected void updateItem(CollecteDechet item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(item.getTypeDechet() + " | " + item.getQuantite() + " kg | " + item.getDateDebut());
                    setStyle(isSelected() ? "-fx-background-color: #aeffae;" : null);  // Changer la couleur de fond si sélectionné
                }
            }
        });

        // Activer la sélection multiple
        collecteListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Filtrage quand le champ de recherche change
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterCollectes(newValue));
    }

    private void filterCollectes(String keyword) {
        final String keywordLower = keyword.toLowerCase(); // Ensure the keyword is final

        List<CollecteDechet> allCollectes = collecteService.getAllCollectes(); // Effectively final
        collecteListView.getItems().setAll(
                allCollectes.stream()
                        .filter(c -> c.getTypeDechet().toLowerCase().contains(keywordLower)) // Use the modified keyword
                        .toList()
        );
    }


    @FXML
    private void enregistrerRecyclage() {
        try {
            double quantite = Double.parseDouble(quantiteRecyclageField.getText());
            double energie = Double.parseDouble(energieProduiteField.getText());
            String utilisation = utilisationEnergieComboBox.getValue();
            LocalDate dateDebut = dateDebutPicker.getValue();
            LocalDate dateFin = dateFinPicker.getValue();

            if (utilisation == null || dateDebut == null || dateFin == null) {
                showAlert("Veuillez remplir tous les champs.");
                return;
            }

            if (dateFin.isBefore(dateDebut)) {
                showAlert("La date de fin ne peut pas être avant la date de début.");
                return;
            }

            if (quantite <= 0 || energie <= 0) {
                showAlert("Les quantités et l'énergie produite doivent être des valeurs positives.");
                return;
            }

            List<CollecteDechet> selectedCollectes = collecteListView.getSelectionModel().getSelectedItems();
            if (selectedCollectes.isEmpty()) {
                showAlert("Veuillez sélectionner au moins une collecte.");
                return;
            }

            RecyclageDechet r = new RecyclageDechet(0, quantite, energie, utilisation, dateDebut, dateFin, imagePath);
            service.ajouterRecyclageAvecCollectes(r, selectedCollectes);

            // 👉 Envoyer un email si la quantité recyclée > 1000
            // 👉 Envoyer un email si la quantité recyclée > 1000
            if (quantite > 1000) {
                String destinataire = "rayenghrairi53@gmail.com"; // L'email de l'admin
                String sujet = "🚨 Alerte : Quantité importante recyclée !";

                // Contenu HTML de l'email avec l'image du logo
                String contenu = "<html><body style='margin:0; padding:0; background-color:#e8f5e9;'>"
                        + "<div style='max-width: 600px; margin: 30px auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.1); font-family: Arial, sans-serif;'>"
                        + "  <div style='background-color: #4CAF50; padding: 20px; text-align: center;'>"
                        + "    <h1 style='color: white; font-size: 24px; margin: 0;'>🚨 Alerte : Quantité importante recyclée</h1>"
                        + "  </div>"
                        + "  <div style='padding: 25px;'>"
                        + "    <p style='font-size: 16px;'><strong>Quantité recyclée :</strong> <span style='color: #2e7d32;'>" + quantite + " kg</span></p>"
                        + "    <p style='font-size: 16px;'><strong>Période :</strong> <span style='color: #1976d2;'>" + dateDebut + " au " + dateFin + "</span></p>"
                        + "    <p style='font-size: 16px;'><strong>Énergie produite :</strong> <span style='color: #ef6c00;'>" + energie + " kWh</span></p>"
                        + "    <p style='font-size: 16px;'><strong>Utilisation prévue :</strong> <span style='color: #8e24aa;'>" + utilisation + "</span></p>"
                        + "    <hr style='margin: 25px 0; border: none; border-top: 1px solid #ccc;'>"
                        + "    <p style='color: #555; font-size: 15px;'>Merci de vérifier cette opération de recyclage et de confirmer si tout est en ordre.</p>"
                        + "  </div>"
                        + "  <div style='background-color: #f1f8e9; padding: 15px; text-align: center; font-size: 13px; color: #777;'>"
                        + "    Équipe <strong>DataFarm</strong> | <a href='mailto:contact@datafarm.com' style='color: #4CAF50; text-decoration: none;'>Nous contacter</a>"
                        + "  </div>"
                        + "</div>"
                        + "</body></html>";



                // Utilisation de ton service d'envoi d'email pour envoyer l'email HTML
                MailUtil.envoyerEmail(destinataire, sujet, contenu);
            }




            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Veuillez entrer des valeurs numériques valides.");
        } catch (SQLException e) {
            showAlert("Erreur de base de données: " + e.getMessage());
        } catch (Exception e) {
            showAlert("Une erreur est survenue : " + e.getMessage());
        }
    }


    private void closeWindow() {
        // Fermer la fenêtre après l'ajout
        Stage stage = (Stage) quantiteRecyclageField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void annuler() {
        // Fermer la fenêtre sans enregistrer
        ((Stage) quantiteRecyclageField.getScene().getWindow()).close();
    }

    @FXML
    private void choisirImage() {
        // Ouvrir un FileChooser pour choisir une image
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath(); // Stocker le chemin de l'image
            // Afficher l'image dans l'ImageView
            Image image = new Image(selectedFile.toURI().toString());
            imageView.setImage(image);
        }
    }

    private void showAlert(String message) {
        // Afficher une alerte avec un message d'erreur
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    @FXML
    public void filterCollectes(KeyEvent keyEvent) {
        // Récupérer le texte du champ de recherche
        String keyword = searchField.getText().toLowerCase();

        // Obtenir toutes les collectes
        List<CollecteDechet> allCollectes = collecteService.getCollectesSansRecyclage();

        // Filtrer les collectes en fonction du texte saisi
        List<CollecteDechet> filteredCollectes = allCollectes.stream()
                .filter(c -> c.getTypeDechet().toLowerCase().contains(keyword)) // Filtrer par type de déchet
                .toList();

        // Mettre à jour la ListView avec les collectes filtrées
        collecteListView.getItems().setAll(filteredCollectes);
    }

}
