package InMemoryDAO;

import DAO.NotificationDAO;
import entity.NotificationEntity;
import utils.SessionManager;

import java.util.*;

public class InMemoryNotificationDAO implements NotificationDAO {
    private final List<NotificationEntity> notifications = new ArrayList<>();
    private int nextId = 1;

    private String getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser().getUsername();
    }

    @Override
    public void saveNotification(NotificationEntity notification) {
        if (notification.getId() == 0) {
            NotificationEntity newNotif = new NotificationEntity(
                    nextId++,
                    notification.getMessage(),
                    notification.getDate(),
                    notification.getPartName(),
                    notification.isHasSuggestedOrder(),
                    notification.getSuggestedQuantity(),
                    notification.getRelatedOrder(),
                    getCurrentUser() // Proprietario
            );
            notifications.add(newNotif);
        } else {
            // Rimuovi vecchio e aggiungi nuovo
            removeNotification(notification);

            notification.setOwnerUsername(getCurrentUser());
            notifications.add(notification);
        }
    }

    @Override
    public List<NotificationEntity> getAllNotifications() {
        return notifications.stream()
                .filter(n -> n.getOwnerUsername().equals(getCurrentUser())) // Solo le mie notifiche
                .toList();
    }

    @Override
    public void clearNotifications() {
        // Rimuove solo le notifiche dell'utente corrente
        notifications.removeIf(n -> n.getOwnerUsername().equals(getCurrentUser()));
    }

    @Override
    public void removeNotification(NotificationEntity notification) {
        notifications.removeIf(n -> n.getId() == notification.getId() &&
                n.getOwnerUsername().equals(getCurrentUser()));
    }
}
