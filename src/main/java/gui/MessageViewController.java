package gui;

import bean.NotificationBean;
import bean.OrderItemBean;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import controller.InventoryManager;
import controller.NotificationManager;
import observer.Observer;
import boundary.NotificationBoundary;

import java.util.ArrayList;
import java.util.List;

public class MessageViewController implements Observer {

    @FXML private Label usernameLabel;
    @FXML private VBox sideMenu;
    @FXML private VBox messagesBox;

    private String loggedUsername;
    private InventoryManager inventoryManager;
    private NotificationManager notificationManager;
    private NotificationBoundary notificationBoundary;

    public void initData(String username, InventoryManager manager, NotificationManager nManager) {
        this.loggedUsername = username;
        this.inventoryManager = manager;
        this.notificationManager = nManager;
        this.notificationBoundary=new NotificationBoundary(notificationManager);

        usernameLabel.setText("HI " + username.toUpperCase());

        // Observer per aggiornamenti
        inventoryManager.addObserver(this);

        refreshMessages();
    }

    private void refreshMessages() {
        messagesBox.getChildren().clear();

        List<NotificationBean> notifications = notificationBoundary.getAllNotifications();

        List<NotificationBean> orderNotifications = new ArrayList<>();
        List<NotificationBean> stockNotifications = new ArrayList<>();

        for (NotificationBean n : notifications) {
            //NOTIFICHE ORDINI: partName = null OPPURE messaggio contiene "Ordine"
            if (n.getPartName() == null ||
                    (n.getMessage() != null &&
                            (n.getMessage().contains("Ordine") || n.getMessage().contains("ORDINE CONSEGNATO")))) {
                orderNotifications.add(n);
            }
            //NOTIFICHE SCORTE: partName != null E messaggio contiene "Scorte basse"
            else if (n.getPartName() != null &&
                    n.getMessage() != null &&
                    n.getMessage().contains("Scorte basse")) {
                stockNotifications.add(n);
            }
        }

        // NOTIFICHE ORDINI
        if (!orderNotifications.isEmpty()) {
            Label orderTitle = new Label("NOTIFICHE ORDINI");
            orderTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #0066cc;");
            messagesBox.getChildren().add(orderTitle);

            for (NotificationBean n : orderNotifications) {
                Label notifLabel = new Label("- " + n.getMessage());
                notifLabel.setWrapText(true);
                notifLabel.setStyle("-fx-text-fill: #0066cc; -fx-padding: 5 0 5 0;");
                messagesBox.getChildren().add(notifLabel);
            }
            messagesBox.getChildren().add(new Label("")); // Spazio
        }

        // NOTIFICHE SCORTE
        if (!stockNotifications.isEmpty()) {
            Label stockTitle = new Label("NOTIFICHE SCORTE");
            stockTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #cc0000;");
            messagesBox.getChildren().add(stockTitle);

            for (NotificationBean n : stockNotifications) {
                Label notifLabel = new Label("- " + n.getMessage());
                notifLabel.setWrapText(true);
                notifLabel.setStyle("-fx-text-fill: #cc0000; -fx-padding: 5 0 5 0;");
                messagesBox.getChildren().add(notifLabel);
            }
            messagesBox.getChildren().add(new Label("")); // Spazio
        }

        // ORDINE SUGGERITO
        List<OrderItemBean> suggestedOrder = notificationBoundary.getSuggestedOrderItems();
        if (!suggestedOrder.isEmpty()) {
            Label orderTitle = new Label("ORDINE SUGGERITO");
            orderTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #008800;");
            messagesBox.getChildren().add(orderTitle);

            for (OrderItemBean item : suggestedOrder) {
                Label orderLine = new Label("• " + item.getPartName() + " x " + item.getQuantity());
                orderLine.setStyle("-fx-padding: 3 0 3 0;");
                messagesBox.getChildren().add(orderLine);
            }

            // Pulsanti azione
            HBox buttonsBox = new HBox(10);
            buttonsBox.setPadding(new Insets(10, 0, 0, 0));

            Button confirmBtn = new Button("Conferma");
            confirmBtn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
            confirmBtn.setOnAction(e -> goToOrderWithSuggested(e, false));

            Button modifyBtn = new Button("Modifica");
            modifyBtn.setStyle("-fx-background-color: gold; -fx-text-fill: black;");
            modifyBtn.setOnAction(e -> goToOrderWithSuggested(e, true));

            buttonsBox.getChildren().addAll(confirmBtn, modifyBtn);
            messagesBox.getChildren().add(buttonsBox);
        }

        // MESSAGGIO VUOTO
        if (notifications.isEmpty() && suggestedOrder.isEmpty()) {
            Label emptyLabel = new Label("Nessuna notifica presente");
            emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray; -fx-padding: 20 0 0 0;");
            messagesBox.getChildren().add(emptyLabel);
        }
    }

    private void goToOrderWithSuggested(ActionEvent event, boolean editMode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/orderView.fxml"));
            Parent root = loader.load();

            OrderViewController controller = loader.getController();
            controller.initData(
                    loggedUsername,
                    inventoryManager,
                    notificationManager,
                    new ArrayList<>(notificationBoundary.getAllNotifications()),
                    editMode
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(NotificationBean notification) {
        Platform.runLater(this::refreshMessages);
    }

    // NAVIGAZIONE
    @FXML private void goToHome(ActionEvent event) { loadView("/fxml/GarageHomeView.fxml", event); }
    @FXML private void goToInventory(ActionEvent event) { loadView("/fxml/InventoryView.fxml", event); }
    @FXML private void goToOrder(ActionEvent event) { loadOrderViewWithParams(event, new ArrayList<>(notificationBoundary.getAllNotifications()), false); }
    @FXML private void goToMessages(ActionEvent event) { /* già qui */ }
    @FXML private void goBack(ActionEvent event) { loadView("/fxml/GarageHomeView.fxml", event); }

    @FXML private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/mainView1.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Smart Garage");
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleClose(ActionEvent event) { Platform.exit(); }

    @FXML
    private void handleRefresh() {
        refreshMessages();
    }

    @FXML private void toggleSideMenu() { sideMenu.setVisible(!sideMenu.isVisible()); }

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