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
        // Logica specifica di FileDataLoader
        if(!inventoryManager.getAllParts().isEmpty()){
            System.out.println("Dati già caricati, skip...");
            return;
        }

        loadCommonParts();
        loadCommonSuppliers();
        loadCommonUsers();

        // Logica specifica per notifiche
        notificationManager.clearNotifications();
        generateCommonNotifications();
    }
}