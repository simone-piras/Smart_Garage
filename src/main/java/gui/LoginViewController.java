package gui;

import boundary.UserBoundary;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import controller.GoogleLoginManager;
import controller.InventoryManager;
import controller.NotificationManager;
import utils.SharedManagers;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

@SuppressWarnings("java:S106")  //Soppressione warning per System.out/System.err
public class LoginViewController {

    private final UserBoundary userBoundary = new UserBoundary();

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button googleLoginButton;

    @FXML
    private Button closeButton;

    @FXML
    private Button backButton;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
        //Singleton per istanze condivise
        NotificationManager sharedNotificationManager = SharedManagers.getInstance().getNotificationManager();
        InventoryManager sharedInventoryManager = SharedManagers.getInstance().getInventoryManager();

        String username = usernameField.getText();
        String password = passwordField.getText();

        boolean success = userBoundary.loginUser(username, password);

        if (success) {
            System.out.println("Login riuscito");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GarageHomeView.fxml"));
                Parent root = loader.load();
                //dependency injection, passo le istanze condivise al prossimo controller e si avranno le stesse istanze grazie a singleton tramite loadView
                GarageHomeController controller = loader.getController();
                controller.initData(username, sharedInventoryManager, sharedNotificationManager);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Your Garage");
                stage.show();
            } catch (Exception e) {
                System.err.println("Errore durante il login: " + e.getMessage());
            }
        } else {
            showError("Credenziali errate o account non registrato");
        }
    }



    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        try {
            GoogleLoginManager googleLoginManager = new GoogleLoginManager();
            String email = googleLoginManager.getEmailFromGoogle();

            boolean success = userBoundary.loginWithGoogle(email);

            if (!success) {
                showError("Credenziali errate o account non registrato");
                return;
            }

            // Recuperiamo lo username dalla sessione appena creata dal manager
            String username = utils.SessionManager.getInstance().getCurrentUser().getUsername();

            System.out.println("Login Google riuscito per " + username);

            //Caricamento della Home
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GarageHomeView.fxml"));
            Parent root = loader.load();

            NotificationManager sharedNotificationManager = SharedManagers.getInstance().getNotificationManager();
            InventoryManager sharedInventoryManager = SharedManagers.getInstance().getInventoryManager();

            GarageHomeController controller = loader.getController();
            controller.initData(username, sharedInventoryManager, sharedNotificationManager);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Your Garage");
            stage.show();

        } catch (Exception e) {
            System.err.println("Errore: " + e.getMessage());
            showError("Errore login Google.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill:red;");
        errorLabel.setVisible(true);
        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(3));
        pauseTransition.setOnFinished(e -> errorLabel.setVisible(false));
        pauseTransition.play();
    }

    @FXML
    private void handleClose(ActionEvent event) {
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/mainView1.fxml"));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Smart Garage");
        } catch(Exception e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }
}
