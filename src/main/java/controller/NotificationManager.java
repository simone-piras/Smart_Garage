package controller;

import bean.NotificationBean;
import bean.PartBean;
import DAO.NotificationDAO;
import entity.NotificationEntity;
import mapper.BeanEntityMapperFactory;
import observer.Observer;
import utils.ApplicationContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// ✅ IMPLEMENTA OBSERVER
public class NotificationManager implements Observer {
    private final NotificationDAO notificationDAO;
    private final BeanEntityMapperFactory mapperFactory = BeanEntityMapperFactory.getInstance();

    public NotificationManager() {
        this.notificationDAO = ApplicationContext.getInstance().getDAOFactory().getNotificationDAO();
    }

    // ✅ METODO OBSERVER - RICEVE NOTIFICHE DA InventoryManager
    @Override
    public void update(NotificationBean notification) {
        // ✅ QUESTO SOSTITUISCE I METODI refreshLowStockNotifications
        // Riceve la notifica già creata da InventoryManager e la salva
        if (notification.getPartName() != null && notification.isHasSuggestedOrder()) {
            addNotification(notification);
        }
    }

    public void addNotification(NotificationBean notificationBean) {
        List<NotificationEntity> existing = notificationDAO.getAllNotifications();
        NotificationEntity entity = mapperFactory.toEntity(notificationBean, NotificationEntity.class);

        // ✅ LOGICA BUSINESS - INVARIATA
        if (notificationBean.getRelatedOrder() != null) {
            boolean orderExists = existing.stream()
                    .anyMatch(n -> n.getRelatedOrder() != null &&
                            n.getRelatedOrder().getOrderID().equals(notificationBean.getRelatedOrder().getOrderID()) &&
                            n.getMessage().equals(notificationBean.getMessage()));

            if (!orderExists) {
                notificationDAO.saveNotification(entity);
            }
        } else if (notificationBean.getPartName() != null) {
            boolean partExists = existing.stream()
                    .anyMatch(n -> n.getPartName() != null &&
                            n.getPartName().equals(notificationBean.getPartName()) &&
                            n.isHasSuggestedOrder() == notificationBean.isHasSuggestedOrder());

            if (!partExists) {
                notificationDAO.saveNotification(entity);
            }
        } else {
            boolean genericExists = existing.stream()
                    .anyMatch(n -> n.getMessage().equals(notificationBean.getMessage()) &&
                            n.getDate().equals(notificationBean.getDate()));

            if (!genericExists) {
                notificationDAO.saveNotification(entity);
            }
        }
    }

    public List<NotificationBean> getAllNotifications() {
        return notificationDAO.getAllNotifications().stream()
                .map(entity -> mapperFactory.toBean(entity, NotificationBean.class))
                .toList();
    }

    public void clearNotifications() {
        notificationDAO.clearNotifications();
    }

    public void removeNotificationsByPartName(String partName) {
        List<NotificationEntity> allNotifications = notificationDAO.getAllNotifications();
        for (NotificationEntity n : new ArrayList<>(allNotifications)) {
            if (partName.equals(n.getPartName())) {
                notificationDAO.removeNotification(n);
            }
        }
    }

    // ❌ ELIMINATI COMPLETAMENTE - NON SERVONO PIÙ
    // public void refreshLowStockNotifications() { ... }
    // public void refreshLowStockNotificationsForPart(String partName) { ... }
}