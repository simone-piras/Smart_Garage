package boundary;

import bean.NotificationBean;
import bean.OrderItemBean;
import controller.NotificationManager;
import utils.SharedManagers;

import java.util.ArrayList;
import java.util.List;

public class NotificationBoundary {
    private final NotificationManager notificationManager;

    //costruttore CLI
    public NotificationBoundary(){
        this.notificationManager = SharedManagers.getInstance().getNotificationManager();
    }

    /*
    Costruttore GUI: dependency injection, permette di condividere la stessa istanza di NotificationManager tra diverse boundary
    nella stessa sessione GUI
     */
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
}