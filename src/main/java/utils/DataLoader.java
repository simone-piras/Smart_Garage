package utils;

public class DataLoader extends AbstractDataLoader {

    public DataLoader() {
        super();
    }

    @Override
    public void load() {
        loadCommonUsers(); //Crea utenti e LOGGA come admin

        loadCommonSuppliers(); // Carica fornitori
        loadCommonParts();     // Carica pezzi (assegnandoli all'admin)

        if (notificationManager.getAllNotifications().isEmpty()) {
            generateCommonNotifications();
        }

        //Logout alla fine del caricamento
        SessionManager.getInstance().logout();
    }
}