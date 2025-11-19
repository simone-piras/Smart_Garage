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

    //recupera tutte le notifiche
    public List<NotificationBean> getAllNotifications() {
        return notificationManager.getAllNotifications();
    }

    public List<OrderItemBean> getSuggestedOrderItems() {
        List<NotificationBean> notifications = getAllNotifications();//recupera tutte le notifiche
        List<OrderItemBean> suggestedItems = new ArrayList<>();
        for (NotificationBean n : notifications) {
            if (n.isHasSuggestedOrder()) { //filtra solo le notifiche con suggerimenti
                OrderItemBean item = new OrderItemBean(); //trasforma notifica in OrderBean
                item.setPartName(n.getPartName());
                item.setQuantity(n.getSuggestedQuantity());
                suggestedItems.add(item);
            }
        }
        return suggestedItems; //ritorna lista pronta per creare un ordine
    }

    public void addNotification(NotificationBean notificationBean){
        notificationBean.validate();
        notificationManager.addNotification(notificationBean);//salva la notifica
    }
}