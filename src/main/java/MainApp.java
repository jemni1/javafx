import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Utiliser Parent comme type générique qui peut contenir n'importe quel layout
        Parent root = FXMLLoader.load(getClass().getResource("RecyclageView.fxml"));

        Scene scene = new Scene(root);
        primaryStage.setTitle("Gestion des Collectes de Déchets");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}