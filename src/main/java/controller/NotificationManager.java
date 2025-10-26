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

public class NotificationManager {
    private final NotificationDAO notificationDAO;
    private final List<Observer> observers = new ArrayList<>();
    private final BeanEntityMapperFactory mapperFactory = BeanEntityMapperFactory.getInstance();

    public NotificationManager() {
        this.notificationDAO = ApplicationContext.getInstance().getDAOFactory().getNotificationDAO();
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

    public void refreshLowStockNotifications() {
        // ✅ LOGICA BUSINESS - INVARIATA
        List<NotificationEntity> allNotifications = notificationDAO.getAllNotifications();
        for (NotificationEntity n : new ArrayList<>(allNotifications)) {
            if (n.getPartName() != null && n.isHasSuggestedOrder()) {
                notificationDAO.removeNotification(n);
            }
        }

        InventoryManager inventoryManager = new InventoryManager();
        List<PartBean> lowStockParts = inventoryManager.getLowStockParts();

        for (PartBean part : lowStockParts) {
            String msg = "Scorte basse per la parte: " + part.getName()
                    + " (Quantità attuale: " + part.getQuantity()
                    + ", Soglia minima: " + part.getReorderThreshold() + ")";
            NotificationBean n = new NotificationBean(msg, null, LocalDate.now().toString(), part.getName());
            n.setHasSuggestedOrder(true);
            n.setSuggestedQuantity((part.getReorderThreshold() + 10) - part.getQuantity());

            NotificationEntity entity = mapperFactory.toEntity(n, NotificationEntity.class);
            notificationDAO.saveNotification(entity);
        }
    }

    public void removeNotificationsByPartName(String partName) {
        List<NotificationEntity> allNotifications = notificationDAO.getAllNotifications();
        for (NotificationEntity n : new ArrayList<>(allNotifications)) {
            if (partName.equals(n.getPartName())) {
                notificationDAO.removeNotification(n);
            }
        }
    }

    public void refreshLowStockNotificationsForPart(String partName) {
        // ✅ LOGICA BUSINESS - INVARIATA
        List<NotificationEntity> allNotifications = notificationDAO.getAllNotifications();
        for (NotificationEntity n : new ArrayList<>(allNotifications)) {
            if (n.getPartName() != null && n.getPartName().equals(partName) && n.isHasSuggestedOrder()) {
                notificationDAO.removeNotification(n);
            }
        }

        InventoryManager inventoryManager = new InventoryManager();
        Optional<PartBean> part = inventoryManager.getAllParts().stream()
                .filter(p -> p.getName().equals(partName))
                .findFirst();

        if (part.isPresent() && part.get().getQuantity() <= part.get().getReorderThreshold()) {
            PartBean p = part.get();
            String msg = "Scorte basse per la parte: " + p.getName()
                    + " (Quantità attuale: " + p.getQuantity()
                    + ", Soglia minima: " + p.getReorderThreshold() + ")";
            NotificationBean n = new NotificationBean(msg, null, LocalDate.now().toString(), p.getName());
            n.setHasSuggestedOrder(true);
            n.setSuggestedQuantity((p.getReorderThreshold() + 10) - p.getQuantity());

            NotificationEntity entity = mapperFactory.toEntity(n, NotificationEntity.class);
            notificationDAO.saveNotification(entity);
        }
    }
}
