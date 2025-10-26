package boundary;

import bean.NotificationBean;
import bean.OrderItemBean;
import controller.NotificationManager;

import java.util.ArrayList;
import java.util.List;

public class NotificationBoundary {
    private final NotificationManager notificationManager;

    public NotificationBoundary(){
        this.notificationManager = new NotificationManager();
    }

    public NotificationBoundary(NotificationManager sharedNotificationManager){
        this.notificationManager = sharedNotificationManager;
    }

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

    public void refreshLowStockNotificationsForPart(String partName) {
        notificationManager.refreshLowStockNotificationsForPart(partName);
    }

    // 🔹 Metodo per aggiornare manualmente le notifiche
    public void refreshLowStockNotifications() {
        notificationManager.refreshLowStockNotifications();
    }

    // 🔹 Metodo per svuotare tutte le notifiche
    public void clearNotifications() {
        notificationManager.clearNotifications();
    }
}
