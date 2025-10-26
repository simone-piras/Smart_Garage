package DAO;

import entity.NotificationEntity;
import java.util.List;

public interface NotificationDAO {
    void saveNotification(NotificationEntity notification);
    List<NotificationEntity> getAllNotifications();
    void clearNotifications();
    void removeNotification(NotificationEntity notification);
}