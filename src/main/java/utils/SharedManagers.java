package utils;

import controller.InventoryManager;
import controller.NotificationManager;

public class SharedManagers {
    private static SharedManagers instance;

    private NotificationManager notificationManager;
    private InventoryManager inventoryManager;

    // ✅ COSTRUTTORE PRIVATO - come ApplicationContext
    private SharedManagers() {
        // Inizializzazione lazy - le istanze vengono create solo quando servono
    }

    // ✅ METODO SINGLETON SYNCHRONIZED - come ApplicationContext
    public static synchronized SharedManagers getInstance() {
        if (instance == null) {
            instance = new SharedManagers();
        }
        return instance;
    }

    // ✅ GETTER per NotificationManager
    public NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            notificationManager = new NotificationManager();
        }
        return notificationManager;
    }

    // ✅ GETTER per InventoryManager
    public InventoryManager getInventoryManager() {
        if (inventoryManager == null) {
            inventoryManager = new InventoryManager(getNotificationManager());
        }
        return inventoryManager;
    }
}