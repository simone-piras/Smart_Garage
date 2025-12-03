package gui;

import bean.NotificationBean;
import bean.PartBean;
import boundary.InventoryBoundary;
import exception.InsufficientStockException;
import exception.PartNotFoundException;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import controller.InventoryManager;
import controller.NotificationManager;
import observer.Observer;
import utils.NavigationUtility;
import utils.AlertUtility;

import java.util.ArrayList;
import java.util.List;

public class InventoryViewController implements Observer {

    @FXML private Label usernameLabel;
    @FXML private VBox inventoryListBox;
    @FXML private PieChart inventoryChart;
    @FXML private VBox sideMenu;

    private String loggedUsername;
    private Alert notificationAlert;
    private InventoryManager inventoryManager;
    private NotificationManager notificationManager;
    private InventoryBoundary inventoryBoundary;

    /*
   I metodi initData vengono chiamati dinamicamente tramite reflection quando l'utente naviga tra
   le diverse viste dell'applicazione.
   i controller delle varie schermate ricevono i dati necessari (username, manager, etc.) al momento della loro creazione.
   Esempio pratico:
   Utente clicca su "INVENTORY" in GarageHomeController
   Viene chiamato goToInventory(event)
   Questo chiama loadView("/fxml/inventoryView.fxml", event)
   JavaFX carica il FXML e crea il InventoryViewController
   Tramite reflection viene chiamato initData(username, inventoryManager, notificationManager)
   Il controller ora ha tutti i dati necessari per funzionare
   DYNAMIC BINDING: I controller vengono caricati dinamicamente a runtime
    */
    public void initData(String username, InventoryManager manager, NotificationManager nManager) {
        this.loggedUsername = username;
        this.inventoryManager = manager;
        this.notificationManager = nManager;
        this.inventoryBoundary = new InventoryBoundary(inventoryManager,notificationManager);

        // Registrati come osservatore
        inventoryManager.addObserver(this);

        // Mostra il nome utente
        usernameLabel.setText("HI " + username.toUpperCase());

        // Popola la GUI
        refreshInventory();
    }

    // Aggiorna lista + grafico
    private void refreshInventory() {
        populateInventoryList();
        updatePieChart();
    }

    private void populateInventoryList() {
        inventoryListBox.getChildren().clear();

        List<PartBean> parts = inventoryBoundary.getAllParts();
        for (PartBean part : parts) {
            HBox row = createInventoryRow(part);
            inventoryListBox.getChildren().add(row);
        }
    }

    private HBox createInventoryRow(PartBean part) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(5));
        row.setStyle("-fx-border-color: lightgray; -fx-background-color: #f9f9f9;");

        Label nameLabel = new Label("Name: " + part.getName());
        Label quantityLabel = new Label("Quantity: " + part.getQuantity());
        Label thresholdLabel = new Label("Threshold: " + part.getReorderThreshold());

        Button incrementButton = createIncrementButton(part);
        Button decrementButton = createDecrementButton(part);

        row.getChildren().addAll(nameLabel, quantityLabel, thresholdLabel, incrementButton, decrementButton);
        return row;
    }

    private Button createIncrementButton(PartBean part) {
        Button button = new Button("+");
        button.setStyle("-fx-background-color: lightgreen;");
        button.setOnAction(e -> handleIncrementPart(part));
        return button;
    }

    private Button createDecrementButton(PartBean part) {
        Button button = new Button("-");
        button.setStyle("-fx-background-color: lightcoral;");
        button.setOnAction(e -> handleDecrementPart(part));
        return button;
    }

    private void handleIncrementPart(PartBean part) {
        try {
            if (inventoryBoundary.updatePartQuantity(part.getName(), +1)) {
                refreshInventory();
            }
        } catch (PartNotFoundException _){
            AlertUtility.showError("Parte non trovata: " + part.getName());
        } catch (InsufficientStockException _) {
            AlertUtility.showError("Scorte insufficienti per: " + part.getName());
        }
    }

    private void handleDecrementPart(PartBean part) {
        try {
            if (inventoryBoundary.updatePartQuantity(part.getName(), -1)) {
                refreshInventory();
            }
        } catch(PartNotFoundException _){
            AlertUtility.showError("Parte non trovata: " + part.getName());
        } catch(InsufficientStockException _){
            AlertUtility.showError("scorte insufficienti per: " + part.getName());
        }
    }

    private void updatePieChart() {
        inventoryChart.getData().clear();
        List<PartBean> parts = inventoryBoundary.getAllParts();
        for (PartBean part : parts) {
            inventoryChart.getData().add(new PieChart.Data(part.getName(), part.getQuantity()));
        }
    }

    // OBSERVER
    @Override
    public void update(NotificationBean notification) {
        if (notification == null) return;
        Platform.runLater(() -> {
            if (notificationAlert == null) {
                notificationAlert = new Alert(Alert.AlertType.WARNING);
                notificationAlert.setTitle("Notifica Scorte");
                notificationAlert.setHeaderText("Livello scorte critico!");
                notificationAlert.initOwner(usernameLabel.getScene().getWindow());
            }
            notificationAlert.setContentText(notification.getMessage());

            if (!notificationAlert.isShowing()) {
                notificationAlert.show();

                PauseTransition delay = new PauseTransition(Duration.seconds(3));
                delay.setOnFinished(e -> notificationAlert.close());
                delay.play();
            }
        });
    }

    // MENU LATERALE
    @FXML private void goToHome(ActionEvent event) {
        NavigationUtility.loadView("/fxml/GarageHomeView.fxml", event, loggedUsername, inventoryManager, notificationManager);
    }

    @FXML private void goToInventory(ActionEvent event) { /* già qui - nessuna azione necessaria */ }

    @FXML private void goToOrder(ActionEvent event) {
        NavigationUtility.loadOrderViewWithParams(event, loggedUsername, inventoryManager, notificationManager,
                new ArrayList<>(notificationManager.getAllNotifications()), false);
    }

    @FXML private void goToMessages(ActionEvent event) {
        NavigationUtility.loadView("/fxml/MessagesView.fxml", event, loggedUsername, inventoryManager, notificationManager);
    }

    @FXML private void handleLogout(ActionEvent event) {
        inventoryManager.removeObserver(this);
        NavigationUtility.handleLogout(event);
    }

    // HEADER
    @FXML private void goBack(ActionEvent event) {
        NavigationUtility.loadView("/fxml/GarageHomeView.fxml", event, loggedUsername, inventoryManager, notificationManager);
    }

    @FXML private void handleClose(ActionEvent event) {
        inventoryManager.removeObserver(this);
        Platform.exit(); }

    @FXML private void toggleSideMenu() { sideMenu.setVisible(!sideMenu.isVisible()); }
}