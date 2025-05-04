package edu.connection.controllers;

import edu.connection.models.YoloV8Detector;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ObjectDetectionController implements Initializable {

    @FXML private ImageView imageView;
    @FXML private Button loadButton;
    @FXML private Button detectButton;
    @FXML private Button saveButton;
    @FXML private Label statusLabel;

    private Mat currentImage;
    private Image displayedImage;
    private YoloV8Detector detector;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            String modelPath = new File("src/main/resources/modele/yolov8n.onnx").getAbsolutePath();
            if (!new File(modelPath).exists()) {
                throw new IOException("Fichier modèle introuvable: " + modelPath);
            }
            detector = new YoloV8Detector(modelPath);
            statusLabel.setText("Modèle YOLOv8 chargé avec succès");
        } catch (Exception e) {
            showError("Erreur lors du chargement du modèle", e.getMessage());
            statusLabel.setText("Erreur: Impossible de charger le modèle");
            disableControls();
        }
    }

    @FXML
    private void onLoadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(imageView.getScene().getWindow());
        if (selectedFile != null) {
            try {
                currentImage = Imgcodecs.imread(selectedFile.getAbsolutePath());
                if (currentImage.empty()) {
                    throw new IOException("Image vide ou non supportée");
                }
                displayedImage = matToJavaFXImage(currentImage);
                imageView.setImage(displayedImage);
                detectButton.setDisable(false);
                saveButton.setDisable(true);
                statusLabel.setText("Image chargée: " + selectedFile.getName());
            } catch (Exception e) {
                showError("Erreur de chargement", e.getMessage());
                currentImage = null;
                disableControls();
            }
        }
    }

    @FXML
    private void onDetectObjects(ActionEvent event) {
        if (currentImage == null || currentImage.empty()) {
            showError("Erreur", "Aucune image valide n'est chargée");
            return;
        }

        try {
            statusLabel.setText("Détection en cours...");
            detectButton.setDisable(true);

            // Ajouter un délai pour permettre l'affichage du message
            new Thread(() -> {
                try {
                    String resultat = detector.simpleDetect(currentImage);

                    // Mise à jour de l'interface dans le thread JavaFX
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText("Résultat : " + resultat);

                        // Afficher un message différent selon le résultat
                        if (resultat.equals("good_crop")) {
                            statusLabel.setStyle("-fx-text-fill: green;");
                        } else if (resultat.equals("bad_crop")) {
                            statusLabel.setStyle("-fx-text-fill: red;");
                        } else {
                            statusLabel.setStyle("-fx-text-fill: orange;");
                        }
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        showError("Erreur de détection", e.getMessage());
                        statusLabel.setText("Erreur lors de la détection");
                    });
                } finally {
                    javafx.application.Platform.runLater(() -> detectButton.setDisable(false));
                }
            }).start();

        } catch (Exception e) {
            showError("Erreur de détection", e.getMessage());
            statusLabel.setText("Erreur lors de la détection");
            detectButton.setDisable(false);
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
            alert.setHeaderText("Impossible de charger la page de detection");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    private void goToCommandes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_commandes.fxml"));
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
            alert.setHeaderText("Impossible de charger la page de detection");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    private void onSaveResult(ActionEvent event) {
        if (imageView.getImage() == null) {
            showError("Erreur", "Aucune image à enregistrer");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images PNG", "*.png")
        );

        File file = fileChooser.showSaveDialog(imageView.getScene().getWindow());
        if (file != null) {
            try {
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                ImageIO.write(bufferedImage, "png", file);
                statusLabel.setText("Image enregistrée: " + file.getName());
            } catch (IOException e) {
                showError("Erreur d'enregistrement", e.getMessage());
            }
        }
    }

    private Image matToJavaFXImage(Mat mat) throws IOException {
        org.opencv.core.MatOfByte matOfByte = new org.opencv.core.MatOfByte();
        if (!Imgcodecs.imencode(".png", mat, matOfByte)) {
            throw new IOException("Échec de la conversion de l'image");
        }
        return new Image(new ByteArrayInputStream(matOfByte.toArray()));
    }

    private void disableControls() {
        detectButton.setDisable(true);
        saveButton.setDisable(true);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
