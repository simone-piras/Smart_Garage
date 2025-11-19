package gui;

import bean.NotificationBean;
import bean.OrderBean;
import bean.OrderItemBean;
import enumerations.OrderStatus;
import exception.PartNotFoundException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import boundary.InventoryBoundary;
import boundary.NotificationBoundary;
import boundary.OrderBoundary;
import boundary.SupplierBoundary;
import boundary.UserBoundary;
import controller.InventoryManager;
import controller.NotificationManager;

import java.util.ArrayList;
import java.util.List;

public class OrderViewController {

    @FXML private Label usernameLabel;
    @FXML private VBox orderBoardBox;
    @FXML private Label orderStatusLabel;
    @FXML private VBox sideMenu;

    private String loggedUsername;
    private InventoryManager inventoryManager;
    private NotificationManager notificationManager;
    private List<NotificationBean> suggestedOrder = new ArrayList<>();
    private boolean editMode;
    private OrderBean currentOrder;
    private ComboBox<String> supplierCombo;
    private Button useDefaultSupplierBtn;

    private InventoryBoundary inventoryBoundary;
    private NotificationBoundary notificationBoundary;
    private OrderBoundary orderBoundary;
    private SupplierBoundary supplierBoundary;
    private UserBoundary userBoundary;

    public void initData(String username,
                         InventoryManager invManager,
                         NotificationManager notifManager,
                         List<NotificationBean> suggestedOrder,
                         boolean editMode) {

        this.loggedUsername = username;
        this.inventoryManager = invManager;
        this.notificationManager = notifManager;
        this.suggestedOrder = (suggestedOrder != null) ? suggestedOrder : new ArrayList<>();
        this.editMode = editMode;

        this.inventoryBoundary = new InventoryBoundary(inventoryManager, notificationManager);
        this.notificationBoundary = new NotificationBoundary(notificationManager);
        this.orderBoundary = new OrderBoundary();
        this.supplierBoundary = new SupplierBoundary();
        this.userBoundary = new UserBoundary();

        usernameLabel.setText("HI " + username.toUpperCase());
        loadOrderBoard();
    }

    private void loadOrderBoard() {
        orderBoardBox.getChildren().clear();
        addSupplierSelection();

        if (editMode && suggestedOrder.isEmpty()) {
            orderStatusLabel.setText("Crea il tuo ordine manuale.");
            addEmptyRow();
            addAddRowButton();
            addConfirmButton();
            return;
        }

        for (NotificationBean n : suggestedOrder) {
            if (n.isHasSuggestedOrder()) {
                HBox row = new HBox(10);
                Label partLabel = new Label(n.getPartName() + " x " + n.getSuggestedQuantity());

                if (editMode) {
                    Button minusBtn = new Button("-");
                    Button plusBtn = new Button("+");

                    minusBtn.setOnAction(e -> {
                        int newQty = Math.max(0, n.getSuggestedQuantity() - 1);
                        n.setSuggestedQuantity(newQty);
                        partLabel.setText(n.getPartName() + " x " + newQty);
                    });

                    plusBtn.setOnAction(e -> {
                        int newQty = n.getSuggestedQuantity() + 1;
                        n.setSuggestedQuantity(newQty);
                        partLabel.setText(n.getPartName() + " x " + newQty);
                    });

                    row.getChildren().addAll(partLabel, minusBtn, plusBtn);
                } else {
                    row.getChildren().add(partLabel);
                }

                orderBoardBox.getChildren().add(row);
            }
        }

        if (editMode) addAddRowButton();
        addConfirmButton();
    }

    private void addEmptyRow() {
        HBox newRow = new HBox(10);

        ComboBox<String> nameCombo = new ComboBox<>();
        inventoryBoundary.getAllParts().forEach(part -> nameCombo.getItems().add(part.getName()));
        nameCombo.setEditable(true);

        Spinner<Integer> qtySpinner = new Spinner<>(1, 999, 1);
        newRow.getChildren().addAll(nameCombo, qtySpinner);
        orderBoardBox.getChildren().add(newRow);
    }

    private void addAddRowButton() {
        Button addRowBtn = new Button("+ Aggiungi scorta");
        addRowBtn.setOnAction(e -> addEmptyRow());
        orderBoardBox.getChildren().add(addRowBtn);
    }

    private void addConfirmButton() {
        Button confirmBtn = new Button("Conferma ordine");
        confirmBtn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        confirmBtn.setOnAction(e -> confermaOrdine());
        orderBoardBox.getChildren().add(confirmBtn);
    }

    private void addSupplierSelection() {
        HBox supplierRow = new HBox(10);

        supplierCombo = new ComboBox<>();
        supplierCombo.setPromptText("Seleziona fornitore");
        supplierBoundary.getAllSuppliers().forEach(s -> supplierCombo.getItems().add(s.getName()));

        useDefaultSupplierBtn = new Button("Usa fornitore predefinito");
        var user = userBoundary.getUser(loggedUsername);
        if (user != null && user.getDefaultSupplierName() != null) {
            String defaultSupplier = user.getDefaultSupplierName();
            useDefaultSupplierBtn.setOnAction(e -> supplierCombo.setValue(defaultSupplier));
        } else {
            useDefaultSupplierBtn.setDisable(true);
        }

        supplierRow.getChildren().addAll(new Label("Fornitore:"), supplierCombo, useDefaultSupplierBtn);
        orderBoardBox.getChildren().add(supplierRow);
    }

    @FXML
    private void placeOrder() {
        this.suggestedOrder.clear();
        this.currentOrder = null;
        this.editMode = true;
        orderStatusLabel.setText("Create your order: ");
        orderBoardBox.getChildren().clear();
        loadOrderBoard();
    }

    private void confermaOrdine() {
        if (currentOrder != null) {
            showAlert(Alert.AlertType.INFORMATION, "Ordine già confermato");
            return;
        }

        List<OrderItemBean> items;
        try {
            items = buildOrderItems();
        } catch (PartNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, e.getMessage());
            return;
        }

        if (items.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nessun articolo da confermare.");
            return;
        }

        String supplierName = supplierCombo != null ? supplierCombo.getValue() : null;
        if (supplierName == null || supplierName.isEmpty()) {
            orderStatusLabel.setVisible(true);
            orderStatusLabel.setText("Devi selezionare un fornitore prima di confermare l'ordine.");
            orderStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            return;
        } else {
            orderStatusLabel.setText("");
        }


        currentOrder = orderBoundary.createOrder(loggedUsername, supplierName, items);
        showOrderConfirmationPopup(currentOrder);
        orderBoardBox.getChildren().removeIf(node -> node instanceof Button b && b.getText().contains("Conferma ordine"));
    }

    private List<OrderItemBean> buildOrderItems() throws PartNotFoundException {
        List<OrderItemBean> items = new ArrayList<>();

        for (NotificationBean n : suggestedOrder) {
            if (n.isHasSuggestedOrder() && n.getSuggestedQuantity() > 0) {
                OrderItemBean item = new OrderItemBean();
                item.setPartName(n.getPartName());
                item.setQuantity(n.getSuggestedQuantity());
                items.add(item);
            }
        }

        for (Node node : orderBoardBox.getChildren()) {
            if (node instanceof HBox row) {
                String name = null;
                Integer qty = null;

                for (Node child : row.getChildren()) {
                    if (child instanceof ComboBox<?> cb) {
                        name = ((ComboBox<String>) cb).getValue();
                    } else if (child instanceof Spinner<?> sp) {
                        Object val = sp.getValue();
                        if (val instanceof Integer i) qty = i;
                    }
                }

                if (name != null && !name.isEmpty() && qty != null && qty > 0) {
                    final String finalName = name;
                    boolean exists = inventoryBoundary.getAllParts().stream()
                            .anyMatch(p -> p.getName().equals(finalName));
                    if (!exists) throw new PartNotFoundException("Parte non trovata: " + finalName);


                    OrderItemBean item = new OrderItemBean();
                    item.setPartName(finalName);
                    item.setQuantity(qty);
                    items.add(item);
                }
            }
        }
        return items;
    }

    @FXML
    private void checkOrderStatus() {
        if (currentOrder == null) {
            showAlert(Alert.AlertType.INFORMATION, "Nessun ordine effettuato.");
            return;
        }


        OrderBean order = orderBoundary.getAllOrders().stream()
                .filter(o -> o.getOrderID().equals(currentOrder.getOrderID()))
                .findFirst()
                .orElse(null);

        if (order != null) {
            showAlert(Alert.AlertType.INFORMATION, "Stato attuale: " + order.getStatus());
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Ordine non trovato.");
        }
    }

    private void showOrderConfirmationPopup(OrderBean order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Conferma Ordine");
        dialog.setHeaderText("ORDINE CONFERMATO CON SUCCESSO!");


        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #f0f8ff; -fx-border-color: #0066cc; -fx-border-width: 2px;");


        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20px; -fx-alignment: center;");

        Label successLabel = new Label("Il tuo ordine è stato confermato!");
        successLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0066cc;");

        Label orderIdLabel = new Label("ID Ordine: #" + order.getOrderID());
        orderIdLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label supplierLabel = new Label("Fornitore: " + order.getSupplierName());
        supplierLabel.setStyle("-fx-font-size: 14px;");

        Label statusLabel = new Label("Stato: " + order.getStatus().toString());
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: green;");


        Label itemsLabel = new Label("Articoli ordinati:");
        itemsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        VBox itemsBox = new VBox(5);
        for (OrderItemBean item : order.getItems()) {
            Label itemLabel = new Label("  • " + item.getPartName() + " x " + item.getQuantity());
            itemLabel.setStyle("-fx-font-size: 12px;");
            itemsBox.getChildren().add(itemLabel);
        }

        content.getChildren().addAll(
                successLabel, orderIdLabel, supplierLabel, statusLabel,
                new Separator(), itemsLabel, itemsBox
        );

        dialogPane.setContent(content);


        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(okButton);


        Button okBtn = (Button) dialogPane.lookupButton(okButton);
        okBtn.setStyle("-fx-background-color: #0066cc; -fx-text-fill: white; -fx-font-weight: bold;");


        dialog.showAndWait();

    }

    // NAVIGAZIONE
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

            try {
                controller.getClass()
                        .getMethod("initData", String.class, InventoryManager.class, NotificationManager.class, List.class, boolean.class)
                        .invoke(controller, loggedUsername, inventoryManager, notificationManager, suggestedOrder, editMode);
            } catch (NoSuchMethodException ignored) {}

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void goToHome(ActionEvent e) { loadView("/fxml/GarageHomeView.fxml", e); }
    @FXML private void goToInventory(ActionEvent e) { loadView("/fxml/inventoryView.fxml", e); }
    @FXML private void goToOrder(ActionEvent e) { loadView("/fxml/orderView.fxml", e); }
    @FXML private void goToMessages(ActionEvent e) { loadView("/fxml/messagesView.fxml", e); }
    @FXML private void goBack(ActionEvent e) { loadView("/fxml/GarageHomeView.fxml", e); }

    @FXML private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/mainView1.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Smart Garage");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleClose(ActionEvent e) { Platform.exit(); }
    @FXML private void toggleSideMenu() { sideMenu.setVisible(!sideMenu.isVisible()); }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}