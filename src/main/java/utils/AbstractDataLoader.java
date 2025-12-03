package utils;

import bean.NotificationBean;
import bean.PartBean;
import bean.UserBean;
import boundary.InventoryBoundary;
import boundary.SupplierBoundary;
import boundary.UserBoundary;
import controller.NotificationManager;
import exception.DuplicateUsernameException;

import java.time.LocalDate;

public abstract class AbstractDataLoader {
    protected final InventoryBoundary inventoryBoundary;
    protected final SupplierBoundary supplierBoundary;
    protected final UserBoundary userBoundary;
    protected final NotificationManager notificationManager;

    protected AbstractDataLoader() {
        this.inventoryBoundary = new InventoryBoundary();
        this.supplierBoundary = new SupplierBoundary();
        this.userBoundary = new UserBoundary();
        this.notificationManager = SharedManagers.getInstance().getNotificationManager();
    }

    protected void loadCommonParts() {
        // I pezzi vengono salvati per l'utente attualmente loggato
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

    protected void loadCommonSuppliers() {
        supplierBoundary.addSupplier("AutoRicambi S.p.A.", "contatti@autoricambi.it", "0111234567");
        supplierBoundary.addSupplier("MeccanicaPlus", "info@meccaplus.it", "0109876543");
        supplierBoundary.addSupplier("PartiVeloci", "ordini@partiveloci.it", "0813344556");
        supplierBoundary.addSupplier("Distribuzione Auto Srl", "service@distribauto.it", "0394455667");
    }

    protected void loadCommonUsers() {
        try {
            userBoundary.registerUser("admin", "admin123", "admin@garage.com");
        } catch (DuplicateUsernameException _) {
            // Utente già esistente
        }

        try {
            userBoundary.registerUser("utente", "password", "utente@email.com");
        } catch (DuplicateUsernameException _) {
            // Utente già esistente
        }

        //Logghiamo l'admin per poter caricare i dati!
        // Senza questo, i DAO fallirebbero perché non c'è un utente in sessione.
        UserBean adminUser = new UserBean();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@garage.com");
        SessionManager.getInstance().login(adminUser);
    }

    protected void generateCommonNotifications() {
        // Funziona solo se c'è un utente loggato (admin)
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

    // Metodo astratto che ogni DataLoader deve implementare
    public abstract void load();
}