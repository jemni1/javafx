package edu.connection.controllers;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import edu.connection.services.CommandeService;
import edu.connection.services.EmailService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import edu.connection.entities.Produit;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;

public class PanierPopupController {

    @FXML private VBox produitsVBox;
    @FXML private Label totalLabel;
    @FXML private Button validerCommandeBtn;
    @FXML private Label itemsCountLabel; // Nouveau label pour le compteur

    private int idClient = 1;
    private Map<Produit, Integer> panier;

    public interface PanierChangeCallback {
        void onPanierChanged();
    }

    private PanierChangeCallback panierChangeCallback;

    public void setOnPanierChangeCallback(PanierChangeCallback callback) {
        this.panierChangeCallback = callback;
    }

    public void setPanier(Map<Produit, Integer> panier, int idClient) {
        this.panier = panier;
        this.idClient = idClient;
        rafraichirAffichagePanier();
    }

    public void setPanier(Map<Produit, Integer> panier) {
        this.panier = panier;
        rafraichirAffichagePanier();
    }

    private void rafraichirAffichagePanier() {
        produitsVBox.getChildren().clear();
        double total = 0;
        int itemCount = 0;

        for (Map.Entry<Produit, Integer> entry : panier.entrySet()) {
            Produit produit = entry.getKey();
            int quantite = entry.getValue();
            itemCount += quantite;
            total += produit.getPrix() * quantite;

            // Carte produit
            HBox produitBox = new HBox(15);
            produitBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");
            produitBox.setMaxWidth(Double.MAX_VALUE);

            // Image produit
            ImageView imageView = new ImageView();
            imageView.setFitWidth(60);
            imageView.setFitHeight(60);
            imageView.setStyle("-fx-background-radius: 5;");
            try {
                Image img = new Image(new File("images/" + produit.getImage()).toURI().toString());
                imageView.setImage(img);
            } catch (Exception e) {
                System.out.println("Image non trouvée pour le produit " + produit.getNom());
                e.printStackTrace();
            }

            // Détails produit
            VBox detailsBox = new VBox(5);
            Label nomLabel = new Label(produit.getNom());
            nomLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

            // Correction: cast vers double pour le format %.2f
            Label prixLabel = new Label(String.format("%.2f € x %d", (double)produit.getPrix(), quantite));
            prixLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 14px;");

            // Correction: enlevé "x %d" qui était en trop
            Label totalLabel = new Label(String.format("Total: %.2f €", (double)produit.getPrix() * quantite));
            totalLabel.setStyle("-fx-text-fill: #006400; -fx-font-weight: bold; -fx-font-size: 14px;");

            detailsBox.getChildren().addAll(nomLabel, prixLabel, totalLabel);

            // Contrôles quantité
            HBox controlsBox = new HBox(10);
            controlsBox.setAlignment(Pos.CENTER_RIGHT);

            Spinner<Integer> spinner = new Spinner<>(1, produit.getQuantite(), quantite);
            spinner.setStyle("-fx-font-size: 14px;");
            spinner.setPrefWidth(70);

            spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                panier.put(produit, newVal);
                rafraichirAffichagePanier();
                if (panierChangeCallback != null) {
                    panierChangeCallback.onPanierChanged();
                }
            });

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e53935; -fx-font-size: 12px; -fx-underline: true; -fx-cursor: hand;");

            deleteBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce produit du panier ?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        panier.remove(produit);
                        rafraichirAffichagePanier();
                        if (panierChangeCallback != null) {
                            panierChangeCallback.onPanierChanged();
                        }
                    }
                });
            });

            controlsBox.getChildren().addAll(spinner, deleteBtn);

            // Ajout à la carte
            produitBox.getChildren().addAll(imageView, detailsBox, controlsBox);
            produitsVBox.getChildren().add(produitBox);
        }

        // Mise à jour des totaux
        totalLabel.setText(String.format("%.2f €", total));
        itemsCountLabel.setText(String.valueOf(itemCount));

        // Style conditionnel pour le bouton
        if (panier.isEmpty()) {
            validerCommandeBtn.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 12 0; -fx-background-radius: 5;");
            validerCommandeBtn.setDisable(true);
        } else {
            validerCommandeBtn.setStyle("-fx-background-color: linear-gradient(to right, #006400, #007000); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 12 0; -fx-background-radius: 5;");
            validerCommandeBtn.setDisable(false);
        }
    }
    @FXML
    private void validerCommande() {
        try {
            if (panier == null || panier.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Panier vide", "Votre panier est vide !");
                return;
            }

            CommandeService commandeService = new CommandeService();
            commandeService.validerCommande(panier, idClient);

            double total = panier.entrySet().stream()
                    .mapToDouble(e -> e.getKey().getPrix() * e.getValue())
                    .sum();

            // Envoi email
            EmailService.sendOrderConfirmationEmail("seifaoun3@gmail.com", panier, total);

            // Paiement Stripe

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (total * 100))
                    .setCurrency("eur")
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            showSuccessAlert(intent.getId());

            panier.clear();
            rafraichirAffichagePanier();
            if (panierChangeCallback != null) {
                panierChangeCallback.onPanierChanged();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de la validation: " + e.getMessage());
        } catch (StripeException e) {
            showErrorAlert(e.getMessage());
        }
    }

    private void showSuccessAlert(String paymentIntentId) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Paiement réussi");
        alert.setHeaderText("Votre commande est confirmée !");
        alert.setContentText("ID de transaction: " + paymentIntentId);
        alert.showAndWait();
    }

    private void showErrorAlert(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de paiement");
        alert.setHeaderText("Un problème est survenu");
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}