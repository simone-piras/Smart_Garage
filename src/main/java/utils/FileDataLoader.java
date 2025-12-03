package utils;

import controller.InventoryManager;


@SuppressWarnings("java:S106")
public class FileDataLoader extends AbstractDataLoader {

    private final InventoryManager inventoryManager;

    public FileDataLoader() {
        super();
        this.inventoryManager = SharedManagers.getInstance().getInventoryManager();
    }

    @Override
    public void load() {

        loadCommonUsers();


        boolean partsExist = !inventoryManager.getAllParts().isEmpty();
        boolean suppliersExist = !supplierBoundary.getAllSuppliers().isEmpty();


        if (partsExist && suppliersExist) {
            System.out.println("Tutti i dati su file sono presenti. Skip caricamento.");
            SessionManager.getInstance().logout();
            return;
        }

        //Carichiamo ciò che manca
        if (!partsExist) {
            System.out.println("Caricamento Parti in corso...");
            loadCommonParts();
            // Rigeneriamo le notifiche solo se abbiamo ricaricato le parti
            notificationManager.clearNotifications();
            generateCommonNotifications();
        } else {
            System.out.println("Parti già presenti.");
        }

        if (!suppliersExist) {
            System.out.println("Caricamento Fornitori in corso...");
            loadCommonSuppliers();
        } else {
            System.out.println("Fornitori già presenti.");
        }

        //Logout finale
        SessionManager.getInstance().logout();
    }
}
