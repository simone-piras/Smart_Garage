package utils;

import controller.InventoryManager;
import controller.NotificationManager;

public class SharedManagers {
    private static NotificationManager notificationManager;
    private static InventoryManager inventoryManager;

    public static NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            notificationManager = new NotificationManager();
        }
        return notificationManager;
    }

    public static InventoryManager getInventoryManager() {
        if (inventoryManager == null) {
            inventoryManager = new InventoryManager(getNotificationManager());
        }
        return inventoryManager;
    }
}
