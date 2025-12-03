package utils;

import bean.NotificationBean;
import controller.InventoryManager;
import controller.NotificationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

@SuppressWarnings("java:S106")  //Soppressione warning per System.err

public class NavigationUtility {

    private NavigationUtility() {
        // Classe utility - non istanziabile
    }

    public static void loadView(String fxmlPath, ActionEvent event,
                                String username, InventoryManager inventoryManager,
                                NotificationManager notificationManager) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtility.class.getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            invokeInitDataMethod(controller, username, inventoryManager, notificationManager);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Errore nel caricamento della vista " + fxmlPath + ": " + e.getMessage());
        }
    }


    public static void loadView(String fxmlPath, ActionEvent event, String title) {
        try {
            Parent root = FXMLLoader.load(NavigationUtility.class.getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            if (title != null) {
                stage.setTitle(title);
            }
            stage.show();
        } catch (Exception e) {
            System.err.println("Errore nel caricamento della vista semplice " + fxmlPath + ": " + e.getMessage());
        }
    }

    public static void loadOrderViewWithParams(ActionEvent event,
                                               String username, InventoryManager inventoryManager,
                                               NotificationManager notificationManager,
                                               List<NotificationBean> suggestedOrder, boolean editMode) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtility.class.getResource("/fxml/orderView.fxml"));
            Parent root = loader.load();

            var controller = loader.getController();
            controller.getClass()
                    .getMethod("initData", String.class, InventoryManager.class,
                            NotificationManager.class, List.class, boolean.class)
                    .invoke(controller, username, inventoryManager, notificationManager,
                            suggestedOrder, editMode);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Errore nel caricamento della vista ordini: " + e.getMessage());
        }
    }

    public static void invokeInitDataMethod(Object controller, String username,
                                            InventoryManager inventoryManager,
                                            NotificationManager notificationManager) {
        try {
            controller.getClass()
                    .getMethod("initData", String.class, InventoryManager.class, NotificationManager.class)
                    .invoke(controller, username, inventoryManager, notificationManager);
        } catch (NoSuchMethodException _) {
            // Il controller non ha il metodo initData, è normale per alcune viste
        } catch (Exception e) {
            System.err.println("Errore durante l'invocazione di initData: " + e.getMessage());

        }
    }

    public static void handleLogout(ActionEvent event) {
        try {
            utils.SessionManager.getInstance().logout();
            Parent root = FXMLLoader.load(NavigationUtility.class.getResource("/fxml/mainView1.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Smart Garage");
            stage.show();
        } catch (Exception e) {
            System.err.println("Errore durante il logout: " + e.getMessage());
        }
    }
}
