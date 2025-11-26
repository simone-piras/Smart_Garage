package gui;

import boundary.UserBoundary;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import controller.InventoryManager;
import controller.NotificationManager;
import utils.NavigationUtility;

import java.util.ArrayList;


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

    @FXML private void goToInventory(ActionEvent event) {
        NavigationUtility.loadView("/fxml/inventoryView.fxml", event, loggedUsername, inventoryManager, notificationManager);
    }

    @FXML private void goToOrder(ActionEvent event) {
        NavigationUtility.loadOrderViewWithParams(event, loggedUsername, inventoryManager, notificationManager,
                new ArrayList<>(notificationManager.getAllNotifications()), false);
    }

    @FXML private void goToMessages(ActionEvent event) {
        NavigationUtility.loadView("/fxml/MessagesView.fxml", event, loggedUsername, inventoryManager, notificationManager);
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
        NavigationUtility.handleLogout(event);
    }
}