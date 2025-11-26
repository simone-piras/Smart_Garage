package utils;


public class DataLoader extends AbstractDataLoader {

    public DataLoader() {
        super();
    }

    @Override
    public void load() {
        loadCommonParts();
        loadCommonSuppliers();
        loadCommonUsers();

        // Logica specifica di DataLoader
        if (!notificationManager.getAllNotifications().isEmpty()) {
            return; // Notifiche già generate
        }
        generateCommonNotifications();
    }
}