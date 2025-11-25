package boundary;

import bean.NotificationBean;
import bean.OrderBean;
import bean.OrderItemBean;
import enumerations.OrderStatus;
import controller.OrderManager;
import controller.SupplierManager;
import utils.OrderProcessorThread;
import utils.SharedManagers;

import java.util.List;
import java.util.UUID;

public class OrderBoundary {
    private final OrderManager orderManager = new OrderManager();
    private final SupplierManager supplierManager = new SupplierManager();
    private final NotificationBoundary notificationBoundary = new NotificationBoundary(SharedManagers.getInstance().getNotificationManager());

    /*
     Il parametro username è necessario per tracciare l'autore degli ordini
     e recuperare le preferenze utente (fornitore predefinito).
     Utilizzato sia in CLI che GUI per:
     - Recuperare UserBean via UserBoundary.getUser(username)
     - Impostare fornitore predefinito via SupplierBoundary.setDefaultSupplier()
     - Tracciare chi ha effettuato l'ordine
     */
    @SuppressWarnings("java:S1172") // Parameter 'username' is required for user tracking
    //Crea un ordine manuale
    public OrderBean createOrder(String username, String supplierName, List<OrderItemBean> items) {
        OrderBean order = new OrderBean();//crea l'oggetto ordine
        order.setOrderID(generateOrderID());
        order.setSupplierName(supplierName);
        order.setStatus(OrderStatus.CREATING);
        order.setItems(items);//lista articoli ordinati

        order.validate();

        orderManager.createOrder(order);//salva ordine

        //CREA NOTIFICA CONFERMA ORDINE CON RIEPILOGO
        String confirmationMsg = "Ordine #" + order.getOrderID() + " confermato il " +
                java.time.LocalDate.now().toString() +
                "\nFornitore: " + supplierName;
        NotificationBean confirmationNotif = new NotificationBean(
                confirmationMsg,
                order, //collega notifica all'ordine
                java.time.LocalDate.now().toString(),
                null
        );
        notificationBoundary.addNotification(confirmationNotif);

        new OrderProcessorThread(order).start();
        return order;//ritorna ordine creato
    }

    //Crea un ordine dai suggerimenti delle notifiche usando il fornitore scelto
    public OrderBean createSuggestedOrder(String username, List<OrderItemBean> suggestedItems, String supplierName) {
        if (supplierName == null || supplierName.isBlank()) {
            return null;
        }
        return createOrder(username, supplierName, suggestedItems);
    }

    //Recupera tutti gli ordini
    public List<OrderBean> getAllOrders() {
        return orderManager.getAllOrders();
    }

    //Recupera tutti i nomi dei fornitori
    public List<String> getSupplierNames() {
        return supplierManager.getAllSuppliers()
                .stream()
                .map(s -> s.getName())
                .toList();
    }
    //crea UUID univoco prendendo solo i primi 8 caratteri
    private String generateOrderID() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
