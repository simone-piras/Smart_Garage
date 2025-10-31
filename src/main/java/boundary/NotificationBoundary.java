package boundary;

import bean.NotificationBean;
import bean.OrderItemBean;
import controller.NotificationManager;
import utils.SharedManagers;  // ✅ AGGIUNGI QUESTA IMPORT

import java.util.ArrayList;
import java.util.List;

public class NotificationBoundary {
    private final NotificationManager notificationManager;

    // MODIFICATO
    public NotificationBoundary(){
        this.notificationManager = SharedManagers.getInstance().getNotificationManager();  // ✅ MODIFICATO
    }

    // RIMANE INVARIATO
    public NotificationBoundary(NotificationManager sharedNotificationManager){
        this.notificationManager = sharedNotificationManager;
    }

    // ✅ TUTTI I METODI RIMANGONO INVARIATI
    public List<NotificationBean> getAllNotifications() {
        return notificationManager.getAllNotifications();
    }

    public List<OrderItemBean> getSuggestedOrderItems() {
        List<NotificationBean> notifications = getAllNotifications();
        List<OrderItemBean> suggestedItems = new ArrayList<>();
        for (NotificationBean n : notifications) {
            if (n.isHasSuggestedOrder()) {
                OrderItemBean item = new OrderItemBean();
                item.setPartName(n.getPartName());
                item.setQuantity(n.getSuggestedQuantity());
                suggestedItems.add(item);
            }
        }
        return suggestedItems;
    }

    public void addNotification(NotificationBean notificationBean){
        notificationBean.validate();
        notificationManager.addNotification(notificationBean);
    }


    // ❌ ELIMINATI - NON SERVONO PIÙ
    // public void refreshLowStockNotificationsForPart(String partName) { ... }
    // public void refreshLowStockNotifications() { ... }
    // public void clearNotifications() { ... }
}