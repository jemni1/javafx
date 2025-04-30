package controllers;

import entities.CollecteDechet;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.CollecteService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

public class CollecteController {

    @FXML private ComboBox<String> typeDechetField;
    @FXML private TextField quantiteField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField imageUrlField;

    @FXML private TableView<CollecteDechet> collecteTable;
    @FXML private TableColumn<CollecteDechet, String> typeColumn;
    @FXML private TableColumn<CollecteDechet, Double> quantiteColumn;
    @FXML private TableColumn<CollecteDechet, LocalDate> dateDebutColumn;
    @FXML private TableColumn<CollecteDechet, LocalDate> dateFinColumn;

    private ObservableList<CollecteDechet> collecteList = FXCollections.observableArrayList();
    private CollecteService collecteService = new CollecteService();

    public void initialize() {
        typeDechetField.getItems().addAll("Fumier", "Paille", "Déchets végétaux", "Coques de fruits", "Autre");

        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTypeDechet()));
        quantiteColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getQuantite()).asObject());
        dateDebutColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateDebut()));
        dateFinColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateFin()));

        collecteTable.setItems(collecteList);
        loadCollectes();
    }

    @FXML
    private void ajouterCollecte(ActionEvent event) {
        String typeDechet = typeDechetField.getValue();
        String quantiteText = quantiteField.getText();
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();
        String imageUrl = imageUrlField.getText();

        if (typeDechet == null || typeDechet.trim().isEmpty()) {
            showAlert("Erreur", "Le type de déchet est obligatoire.");
            return;
        }

        if (quantiteText == null || quantiteText.trim().isEmpty()) {
            showAlert("Erreur", "La quantité est obligatoire.");
            return;
        }

        double quantite;
        try {
            quantite = Double.parseDouble(quantiteText);
            if (quantite <= 0) {
                showAlert("Erreur", "La quantité doit être un nombre positif.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La quantité doit être un nombre valide.");
            return;
        }

        if (dateDebut == null) {
            showAlert("Erreur", "La date de début est obligatoire.");
            return;
        }

        if (dateFin == null) {
            showAlert("Erreur", "La date de fin est obligatoire.");
            return;
        }

        if (dateDebut.isAfter(dateFin)) {
            showAlert("Erreur", "La date de début ne peut pas être après la date de fin.");
            return;
        }

        // Copier l'image dans un dossier spécifique
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            if (!(imageUrl.endsWith(".jpg") || imageUrl.endsWith(".jpeg") || imageUrl.endsWith(".png"))) {
                showAlert("Erreur", "L'image doit être au format JPG, JPEG ou PNG.");
                return;
            }

            // Utiliser un dossier dans le répertoire courant du projet
            String destinationFolder = System.getProperty("user.dir") + "/images/";  // Répertoire dans le dossier courant de l'application
            File sourceFile = new File(imageUrl);
            File destFile = new File(destinationFolder + sourceFile.getName());

            // Créer le dossier si nécessaire
            File destinationDir = new File(destinationFolder);
            if (!destinationDir.exists()) {
                destinationDir.mkdirs();
            }

            try {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                imageUrl = "images/" + sourceFile.getName(); // Enregistrer le chemin relatif dans l'entité
            } catch (IOException e) {
                showAlert("Erreur", "Impossible de copier l'image : " + e.getMessage());
                return;
            }
        }

        CollecteDechet collecte = new CollecteDechet(typeDechet, quantite, dateDebut, dateFin, imageUrl);
        collecteService.ajouterCollecte(collecte);

        loadCollectes();
        showAlert("Succès", "Collecte ajoutée avec succès.");
        resetForm();
    }


    @FXML
    private void supprimerCollecte(ActionEvent event) {
        CollecteDechet selectedCollecte = collecteTable.getSelectionModel().getSelectedItem();

        if (selectedCollecte != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Êtes-vous sûr de vouloir supprimer cette collecte ?");
            alert.setContentText("Cette action est irréversible.");

            // Affiche la boîte de dialogue et attend la réponse
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    collecteService.deleteCollecte(selectedCollecte.getId());
                    collecteList.remove(selectedCollecte);
                    showAlert("Succès", "Collecte supprimée avec succès.");
                }
            });
        } else {
            showAlert("Erreur", "Aucune collecte sélectionnée.");
        }
    }


    @FXML
    private void modifierCollecte(ActionEvent event) {
        CollecteDechet selectedCollecte = collecteTable.getSelectionModel().getSelectedItem();
        if (selectedCollecte != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCollecteView.fxml"));
                Parent root = loader.load();

                ModifierCollecteController controller = loader.getController();
                controller.initData(selectedCollecte);

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Modifier Collecte");
                stage.setScene(new Scene(root));
                stage.showAndWait();

                loadCollectes();
            } catch (IOException e) {
                showAlert("Erreur", "Impossible de charger la fenêtre de modification :\n" + e.getMessage());
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner une collecte à modifier.");
        }
    }

    @FXML
    private void afficherDetails(ActionEvent actionEvent) {
        CollecteDechet selected = collecteTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String details = String.format("Type: %s\nQuantité: %.2f kg\nDébut: %s\nFin: %s",
                    selected.getTypeDechet(),
                    selected.getQuantite(),
                    selected.getDateDebut(),
                    selected.getDateFin()
            );

            if (selected.getImageUrl() != null && !selected.getImageUrl().isEmpty()) {
                ImageView imageView = new ImageView(new Image("file:" + selected.getImageUrl()));
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);

                // Affichage dans un dialog
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Détails de la collecte");
                alert.setHeaderText(details);
                alert.setGraphic(imageView);
                alert.showAndWait();
            } else {
                showAlert("Détails de la collecte", details);
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner une collecte pour voir les détails.");
        }
    }

    private void loadCollectes() {
        collecteList.clear();
        collecteList.addAll(collecteService.getAllCollectes());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void resetForm() {
        typeDechetField.setValue(null);
        quantiteField.clear();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        imageUrlField.clear();
    }

    public void choisirImage(ActionEvent actionEvent) {
        // Créer une instance de FileChooser
        FileChooser fileChooser = new FileChooser();


        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png");
        fileChooser.getExtensionFilters().add(imageFilter);


        Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        // Si un fichier est sélectionné, afficher le chemin dans le champ imageUrlField
        if (file != null) {
            imageUrlField.setText(file.getAbsolutePath());
        }
    }
}
