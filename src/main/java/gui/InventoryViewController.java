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
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.util.Duration;
import controller.InventoryManager;
import controller.NotificationManager;
import observer.Observer;

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
            HBox row = new HBox(10);
            row.setPadding(new Insets(5));
            row.setStyle("-fx-border-color: lightgray; -fx-background-color: #f9f9f9;");

            Label nameLabel = new Label("Name: " + part.getName());
            Label quantityLabel = new Label("Quantity: " + part.getQuantity());
            Label thresholdLabel = new Label("Threshold: " + part.getReorderThreshold());

            Button incrementButton = new Button("+");
            incrementButton.setStyle("-fx-background-color: lightgreen;");
            incrementButton.setOnAction(e -> {
                try {
                    if (inventoryBoundary.updatePartQuantity(part.getName(), +1)) {
                        refreshInventory();
                    }
                }catch (PartNotFoundException ex){
                    showError("Parte non trovata: " + part.getName());
                } catch (InsufficientStockException ex) {
                    showError("Scorte insufficienti per: " + part.getName());
                }
            });

            Button decrementButton = new Button("-");
            decrementButton.setStyle("-fx-background-color: lightcoral;");
            decrementButton.setOnAction(e -> {
                try {
                    if (inventoryBoundary.updatePartQuantity(part.getName(), -1)) {
                        refreshInventory();
                    }
                }catch(PartNotFoundException ex){
                    showError("Parte non trovata: " + part.getName());

                }catch(InsufficientStockException ex){
                    showError("scorte insufficienti per: " + part.getName());
                }
            });

            row.getChildren().addAll(nameLabel, quantityLabel, thresholdLabel, incrementButton, decrementButton);
            inventoryListBox.getChildren().add(row);
        }
    }

    private void showError(String message){
        Alert alert=new Alert(Alert.AlertType.ERROR);
        alert.setTitle("errore");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void updatePieChart() {
        inventoryChart.getData().clear();
        List<PartBean> parts = inventoryBoundary.getAllParts();
        for (PartBean part : parts) {
            inventoryChart.getData().add(new PieChart.Data(part.getName(), part.getQuantity()));
        }
    }

    // === OBSERVER ===
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

    // === MENU LATERALE ===
    @FXML private void goToHome(ActionEvent event) { loadView("/fxml/GarageHomeView.fxml", event); }
    @FXML private void goToInventory(ActionEvent event) { /* già qui */ }
    @FXML private void goToOrder(ActionEvent event) { loadOrderViewWithParams(event, new ArrayList<>(notificationManager.getAllNotifications()), false); }
    @FXML private void goToMessages(ActionEvent event) { loadView("/fxml/MessagesView.fxml", event); }

    @FXML private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/mainView1.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Smart Garage");
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // === HEADER ===
    @FXML private void goBack(ActionEvent event) { loadView("/fxml/GarageHomeView.fxml", event); }
    @FXML private void handleClose(ActionEvent event) { Platform.exit(); }
    @FXML private void toggleSideMenu() { sideMenu.setVisible(!sideMenu.isVisible()); }

    // === UTIL ===
    private void loadView(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            try {
                controller.getClass()
                        .getMethod("initData", String.class, InventoryManager.class, NotificationManager.class)
                        .invoke(controller, loggedUsername, inventoryManager, notificationManager);
            } catch (NoSuchMethodException ignored) {}

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadOrderViewWithParams(ActionEvent event, List<NotificationBean> suggestedOrder, boolean editMode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/orderView.fxml"));
            Parent root = loader.load();

            OrderViewController controller = loader.getController();
            controller.initData(loggedUsername, inventoryManager, notificationManager, suggestedOrder, editMode);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}

