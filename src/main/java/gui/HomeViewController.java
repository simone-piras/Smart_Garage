package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class HomeViewController {

    @FXML private Button loginButton;
    @FXML private Button registrationButton;
    @FXML private Button closeButton;

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        loadView("/fxml/loginView.fxml", event, "Login - Smart Garage");
    }

    @FXML
    private void handleRegistration(ActionEvent event) {
        loadView("/fxml/registrationView.fxml", event, "Registration - Smart Garage");
    }

    /**
     * Metodo riutilizzabile per cambiare scena.
     */
    private void loadView(String fxmlPath, ActionEvent event, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
