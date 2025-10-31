package utils;

import bean.NotificationBean;
import bean.PartBean;
import java.time.LocalDate;
import bean.SupplierBean;
import boundary.InventoryBoundary;
import boundary.SupplierBoundary;
import boundary.UserBoundary;
import controller.NotificationManager;

public class DataLoader {

    private final InventoryBoundary inventoryBoundary = new InventoryBoundary();
    private final SupplierBoundary supplierBoundary = new SupplierBoundary();
    private final UserBoundary userBoundary = new UserBoundary();
    private final NotificationManager notificationManager = new NotificationManager();

    public void load() {
        loadParts();
        loadSuppliers();
        loadUsers();
        generateNotifications();
    }

    private void loadParts() {
        inventoryBoundary.addOrUpdatePart("Filtro Olio", 10, 5);
        inventoryBoundary.addOrUpdatePart("Filtro Aria", 4, 3);
        inventoryBoundary.addOrUpdatePart("Pastiglie Freno", 2, 5);
        inventoryBoundary.addOrUpdatePart("Olio Motore", 8, 4);
        inventoryBoundary.addOrUpdatePart("Candele", 3, 3);
        inventoryBoundary.addOrUpdatePart("Batteria Auto", 1, 2);
        inventoryBoundary.addOrUpdatePart("Fanale Posteriore", 7, 2);
        inventoryBoundary.addOrUpdatePart("Cinghia Distribuzione", 6, 4);
        inventoryBoundary.addOrUpdatePart("Liquido Freni", 5, 5);
        inventoryBoundary.addOrUpdatePart("Lampadina Faro", 0, 3);
    }

    private void loadSuppliers() {
        supplierBoundary.addSupplier("AutoRicambi S.p.A.", "contatti@autoricambi.it", "0111234567");
        supplierBoundary.addSupplier("MeccanicaPlus", "info@meccaplus.it", "0109876543");
        supplierBoundary.addSupplier("PartiVeloci", "ordini@partiveloci.it", "0813344556");
        supplierBoundary.addSupplier("Distribuzione Auto Srl", "service@distribauto.it", "0394455667");
    }

    private void loadUsers() {
        userBoundary.registerUser("admin", "admin123", "admin@garage.com");
        userBoundary.registerUser("utente", "password", "utente@email.com");
    }

    private void generateNotifications() {
        if (!notificationManager.getAllNotifications().isEmpty()) {
            return; // Notifiche già generate
        }
        for (PartBean part : inventoryBoundary.getAllParts()) {
            if (part.getQuantity() <= part.getReorderThreshold()) {
                String message = "Scorte basse per la parte: " + part.getName() +
                        " (Quantità attuale: " + part.getQuantity() +
                        ", Soglia minima: " + part.getReorderThreshold() + ")";

                NotificationBean notification = new NotificationBean();
                notification.setMessage(message);
                notification.setDate(LocalDate.now().toString());
                notification.setPartName(part.getName());
                notification.setHasSuggestedOrder(false);
                notification.setSuggestedQuantity(0);
                notification.validate();

                notificationManager.addNotification(notification);
            }
        }
    }
}