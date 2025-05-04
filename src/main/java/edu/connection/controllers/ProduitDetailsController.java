package edu.connection.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import edu.connection.entities.Produit;

import java.io.File;

public class ProduitDetailsController {

    @FXML private ImageView imageView;
    @FXML private Label nomLabel;
    @FXML private Label quantiteLabel;
    @FXML private Label prixLabel;

    public void setProduit(Produit produit) {
        nomLabel.setText("Nom : " + produit.getNom());
        quantiteLabel.setText("Quantité : " + produit.getQuantite());
        prixLabel.setText("Prix : " + produit.getPrix() + " dt");

        try {
            Image image = new Image(new File("images/" + produit.getImage()).toURI().toString());
            imageView.setImage(image);
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/200x100"));
        }
    }
}
