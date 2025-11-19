package gui;

import boundary.UserBoundary;
import bean.UserBean;
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
                e.printStackTrace();
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

            String username = email.split("@")[0];

            UserBean user = userBoundary.getUser(username);

            if (user == null) {
                showError("Credenziali errate o account non registrato");
                return;
            }

            System.out.println("Login Google riuscito per " + username);

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
            e.printStackTrace();
            showError("Errore durante il login con Google.");
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
            e.printStackTrace();
        }
    }
}