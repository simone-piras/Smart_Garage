package InMemoryDAO;

import DAO.NotificationDAO;
import entity.NotificationEntity;
import java.util.*;

public class InMemoryNotificationDAO implements NotificationDAO {
    private final List<NotificationEntity> notifications = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void saveNotification(NotificationEntity notification) {
        if (notification.getId() == 0) {
            // ✅ Costruttore corretto con ID: (id, message, date, partName, hasSuggestedOrder, suggestedQuantity, relatedOrder)
            NotificationEntity newNotif = new NotificationEntity(nextId++, notification.getMessage(),
                    notification.getDate(), notification.getPartName(), notification.isHasSuggestedOrder(),
                    notification.getSuggestedQuantity(), notification.getRelatedOrder());
            notifications.add(newNotif);
        } else {
            // Se ha ID, rimuovi eventuale duplicato e aggiungi
            notifications.removeIf(n -> n.getId() == notification.getId());
            notifications.add(notification);
        }
    }

    @Override
    public List<NotificationEntity> getAllNotifications() {
        return new ArrayList<>(notifications);
    }

    @Override
    public void clearNotifications() {
        notifications.clear();
    }

    @Override
    public void removeNotification(NotificationEntity notification) {
        notifications.removeIf(n -> n.getId() == notification.getId());
    }
}