package gui;

import boundary.UserBoundary;
import exception.DuplicateUsernameException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import controller.GoogleLoginManager;

public class RegistrationViewController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private TextField emailField;

    @FXML
    private Label errorLabel;


    private final UserBoundary userBoundary = new UserBoundary();

    @FXML
    private void handleRegistration(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();

        try{
        userBoundary.registerUser(username, password, email);
        showSuccess("Registrazione riuscita!");
        PauseTransition pause=new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> switchToMainView(event));
        pause.play();
        } catch (DuplicateUsernameException e) {
            showError("Username già registrato", event);

        }


    }

    @FXML
    private void handleGoogleRegistration(ActionEvent event) {
        try {
            GoogleLoginManager googleLoginManager= new GoogleLoginManager();
            String email=googleLoginManager.getEmailFromGoogle();
            String username = email.split("@")[0];

            userBoundary.registerGoogleUser(username, email);
            showSuccess("Registrazione riuscita!");
            PauseTransition pause=new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> switchToMainView(event));
            pause.play();
        } catch (DuplicateUsernameException ex) {
                System.out.println("Utente già esistente");
                showError("Username già registrato con Google", event);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @FXML
    private void handleBack(ActionEvent event) {
        switchToMainView(event);
    }

    @FXML
    private void handleClose(ActionEvent event) {
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }

    private void switchToMainView(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/mainView1.fxml"));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Smart Garage");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message, ActionEvent event) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
        pause.setOnFinished(e -> {
            errorLabel.setVisible(false);
            switchToMainView(event);
        });
        pause.play();
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);                        // Imposta il testo
        errorLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;"); // Verde e bold
        errorLabel.setVisible(true);                        // Mostra il Label

        // Fa scomparire il messaggio dopo 2,5 secondi
        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
        pause.setOnFinished(e -> errorLabel.setVisible(false));
        pause.play();
    }
}
