package gui;

import bean.NotificationBean;
import bean.OrderItemBean;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import controller.InventoryManager;
import controller.NotificationManager;
import observer.Observer;
import boundary.NotificationBoundary;
import utils.NavigationUtility;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("java:S106")//per System.err
public class MessageViewController implements Observer {

    @FXML private Label usernameLabel;
    @FXML private VBox sideMenu;
    @FXML private VBox messagesBox;
    @FXML private Button refreshButton;

    private String loggedUsername;
    private InventoryManager inventoryManager;
    private NotificationManager notificationManager;
    private NotificationBoundary notificationBoundary;

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

        categorizeNotifications(notifications, orderNotifications, stockNotifications);

        displayOrderNotifications(orderNotifications);
        displayStockNotifications(stockNotifications);
        displaySuggestedOrder();
        displayEmptyMessageIfNeeded(notifications);
    }

    private void categorizeNotifications(List<NotificationBean> notifications,
                                         List<NotificationBean> orderNotifications,
                                         List<NotificationBean> stockNotifications) {
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
    }

    private void displayOrderNotifications(List<NotificationBean> orderNotifications) {
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
    }

    private void displayStockNotifications(List<NotificationBean> stockNotifications) {
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
    }

    private void displaySuggestedOrder() {
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
    }

    private void displayEmptyMessageIfNeeded(List<NotificationBean> notifications) {
        List<OrderItemBean> suggestedOrder = notificationBoundary.getSuggestedOrderItems();
        if (notifications.isEmpty() && suggestedOrder.isEmpty()) {
            Label emptyLabel = new Label("Nessuna notifica presente");
            emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray; -fx-padding: 20 0 0 0;");
            messagesBox.getChildren().add(emptyLabel);
        }
    }

    private void goToOrderWithSuggested(ActionEvent event, boolean editMode) {
        try {
            NavigationUtility.loadOrderViewWithParams(event, loggedUsername, inventoryManager, notificationManager,
                    new ArrayList<>(notificationBoundary.getAllNotifications()), editMode);
        } catch (Exception e) {
            System.err.println("Errore nel caricamento ordine suggerito: " + e.getMessage());
        }
    }

    @Override
    public void update(NotificationBean notification) {
        Platform.runLater(this::refreshMessages);
    }

    // NAVIGAZIONE
    @FXML private void goToHome(ActionEvent event) {
        NavigationUtility.loadView("/fxml/GarageHomeView.fxml", event, loggedUsername, inventoryManager, notificationManager);
    }

    @FXML private void goToInventory(ActionEvent event) {
        NavigationUtility.loadView("/fxml/InventoryView.fxml", event, loggedUsername, inventoryManager, notificationManager);
    }

    @FXML private void goToOrder(ActionEvent event) {
        NavigationUtility.loadOrderViewWithParams(event, loggedUsername, inventoryManager, notificationManager,
                new ArrayList<>(notificationBoundary.getAllNotifications()), false);
    }

    @FXML private void goToMessages(ActionEvent event) { /* già qui - nessuna azione necessaria */ }

    @FXML private void goBack(ActionEvent event) {
        NavigationUtility.loadView("/fxml/GarageHomeView.fxml", event, loggedUsername, inventoryManager, notificationManager);
    }

    @FXML private void handleLogout(ActionEvent event) {
        NavigationUtility.handleLogout(event);
    }

    @FXML private void handleClose(ActionEvent event) { Platform.exit(); }

    @FXML
    private void handleRefresh() {
        refreshMessages();
    }

    @FXML private void toggleSideMenu() { sideMenu.setVisible(!sideMenu.isVisible()); }
}