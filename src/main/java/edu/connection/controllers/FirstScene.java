package edu.connection.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import edu.connection.entities.Personne;
import edu.connection.services.PersonneService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class FirstScene {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button tfbtn;

    @FXML
    private TextField tfnom;

    @FXML
    private TextField tfprenom;

    @FXML
    void save(ActionEvent event) {
        String nom = tfnom.getText();
        String prenom = tfprenom.getText();

        Personne personne = new Personne(nom, prenom);
        PersonneService personneService = new PersonneService();
        personneService.addEntity(personne);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/SecondScene.fxml"));
            Parent root =fxmlLoader.load();
            SecondScene secondScene = fxmlLoader.getController();
            secondScene.setResnom(nom);
            secondScene.setResprenom(prenom);
            tfnom.getScene().setRoot(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    void initialize() {

    }

}
