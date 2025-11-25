package gui;

import bean.NotificationBean;
import boundary.UserBoundary;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import controller.InventoryManager;
import controller.NotificationManager;

import java.util.ArrayList;
import java.util.List;

public class GarageHomeController {

    @FXML private VBox sideMenu;
    @FXML private Label userGreetingLabel;
    @FXML private Label defaultSupplierLabel;

    private String loggedUsername;
    private InventoryManager inventoryManager;
    private NotificationManager notificationManager;
    private UserBoundary userBoundary;

    // Metodo per inizializzare i dati dell'utente loggato (dependency injection)
    public void initData(String username, InventoryManager inventoryManager, NotificationManager notificationManager) {
        this.loggedUsername = username;
        this.inventoryManager = inventoryManager;
        this.notificationManager = notificationManager;
        this.userBoundary = new UserBoundary();

        userGreetingLabel.setText("HI " + username.toUpperCase());

        // Recupero fornitore di default tramite boundary
        var user = userBoundary.getUser(username);
        if (user != null && user.getDefaultSupplierName() != null && !user.getDefaultSupplierName().isEmpty()) {
            defaultSupplierLabel.setText("YOUR DEFAULT SUPPLIER: " + user.getDefaultSupplierName());
        }
    }

    // NAVIGAZIONE
    @FXML private void goToHome(ActionEvent event) { /* già nella home */ }
    @FXML private void goToInventory(ActionEvent event) { loadView("/fxml/inventoryView.fxml", event); }
    @FXML private void goToOrder(ActionEvent event) {
        loadOrderViewWithParams(event, new ArrayList<>(notificationManager.getAllNotifications()), false);
    }
    @FXML private void goToMessages(ActionEvent event) { loadView("/fxml/MessagesView.fxml", event); }

    private void loadView(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            invokeInitDataMethod(controller);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void invokeInitDataMethod(Object controller) {
        try {
            controller.getClass()
                    .getMethod("initData", String.class, InventoryManager.class, NotificationManager.class)
                    .invoke(controller, loggedUsername, inventoryManager, notificationManager);
        } catch (NoSuchMethodException _) {
            // Il controller non ha il metodo initData, è normale per alcune viste
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadOrderViewWithParams(ActionEvent event, List<NotificationBean> suggestedOrder, boolean editMode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/orderView.fxml"));
            Parent root = loader.load();

            var controller = loader.getController();
            controller.getClass()
                    .getMethod("initData", String.class, InventoryManager.class, NotificationManager.class, List.class, boolean.class)
                    .invoke(controller, loggedUsername, inventoryManager, notificationManager, suggestedOrder, editMode);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // PULSANTE HAMBURGER
    @FXML private void toggleSideMenu() { sideMenu.setVisible(!sideMenu.isVisible()); }

    // PULSANTE X
    @FXML private void handleClose(ActionEvent event) { ((Stage)((Node)event.getSource()).getScene().getWindow()).close(); }

    // SET DEFAULT SUPPLIER
    @FXML private void handleSetDefault1() { setDefaultSupplier("AutoRicambi S.p.A."); }
    @FXML private void handleSetDefault2() { setDefaultSupplier("MeccanicaPlus"); }
    @FXML private void handleSetDefault3() { setDefaultSupplier("Distribuzione Auto Srl"); }

    private void setDefaultSupplier(String supplierName) {
        if (loggedUsername != null) {
            userBoundary.setDefaultSupplier(loggedUsername, supplierName);
            defaultSupplierLabel.setText("YOUR DEFAULT SUPPLIER: " + supplierName);
        }
    }

    @FXML private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/mainView1.fxml"));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Smart Garage");
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}