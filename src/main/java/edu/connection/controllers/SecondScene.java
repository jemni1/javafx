package edu.connection.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class SecondScene {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField resnom;

    @FXML
    private TextField resprenom;

    public void setResnom(String resnom) {
        this.resnom.setText(resnom);
    }
    public void setResprenom(String resprenom) {
        this.resprenom.setText(resprenom);
    }

    @FXML
    void initialize() {

    }

}
