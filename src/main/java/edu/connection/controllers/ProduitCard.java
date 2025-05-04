package edu.connection.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import edu.connection.entities.Produit;
import edu.connection.services.ProduitService;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class ProduitCard {

    @FXML private ImageView imageView;
    @FXML private Label nomLabel;
    @FXML private Label quantiteLabel;
    @FXML private Label prixLabel;
    @FXML private Button deleteButton;
    @FXML private Button editButton;
    @FXML private Button detailsButton;

    private Produit produit;
    private final ProduitService produitService = new ProduitService();

    @FXML
    private void initialize() {
        detailsButton.setOnAction(e -> openDetails());
        deleteButton.setOnAction(e -> confirmAndDelete());
        editButton.setOnAction(e -> openEditForm());
    }

    public void setData(Produit produit) {
        this.produit = produit;
        nomLabel.setText(produit.getNom());
        quantiteLabel.setText("Quantité: " + produit.getQuantite());
        prixLabel.setText("Prix: " + produit.getPrix() + " dt");

        try {
            Image image = new Image(new File("images/" + produit.getImage()).toURI().toString());
            imageView.setImage(image);
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/200x100"));
        }
    }

    private void openDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/produit_details.fxml"));
            VBox content = loader.load();
            ProduitDetailsController controller = loader.getController();
            controller.setProduit(produit);
            showPopup("Détails du produit", content);
        } catch (IOException ex) {
            handleException(ex);
        }
    }

    private void confirmAndDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce produit ?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                produitService.deleteEntity(produit);
                removeCardFromUI();
            }
        });
    }

    private void removeCardFromUI() {
        VBox card = (VBox) deleteButton.getParent().getParent(); // Button -> HBox -> VBox
        ((FlowPane) card.getParent()).getChildren().remove(card);
    }

    private void openEditForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_produit.fxml"));
            VBox content = loader.load();
            EditProduitController controller = loader.getController();
            controller.setProduit(produit);

            controller.setOnUpdateCallback(() -> reloadMainView("/listeProduits.fxml"));

            showPopup("Modifier Produit", content);
        } catch (IOException ex) {
            handleException(ex);
        }
    }

    private void reloadMainView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = (Stage) editButton.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
        } catch (IOException ex) {
            handleException(ex);
        }
    }

    private void showPopup(String title, VBox content) {
        Stage popupStage = new Stage();
        popupStage.setTitle(title);
        popupStage.setScene(new Scene(content));
        popupStage.initOwner(editButton.getScene().getWindow());
        popupStage.show();
    }

    private void handleException(Exception ex) {
        ex.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR, "Une erreur est survenue : " + ex.getMessage());
        alert.showAndWait();
    }
}
