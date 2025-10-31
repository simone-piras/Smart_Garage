package boundary;

import bean.NotificationBean;
import bean.OrderBean;
import bean.OrderItemBean;
import enumerations.OrderStatus;
import controller.OrderManager;
import controller.SupplierManager;
import controller.UserManager;
import utils.OrderProcessorThread;
import utils.SharedManagers;

import java.util.List;
import java.util.UUID;

public class OrderBoundary {
    private final OrderManager orderManager = new OrderManager();
    private final UserManager userManager = new UserManager();
    private final SupplierManager supplierManager = new SupplierManager();
    private final NotificationBoundary notificationBoundary = new NotificationBoundary(SharedManagers.getNotificationManager());

    /** Crea un ordine manuale */
    public OrderBean createOrder(String username, String supplierName, List<OrderItemBean> items) {
        OrderBean order = new OrderBean();
        order.setOrderID(generateOrderID());
        order.setSupplierName(supplierName);
        order.setStatus(OrderStatus.CREATING);
        order.setItems(items);

        order.validate();

        orderManager.createOrder(order);

        // 🔹 NOTIFICA CONFERMA ORDINE CON RIEPILOGO
        String confirmationMsg = "Ordine #" + order.getOrderID() + " confermato il " +
                java.time.LocalDate.now().toString() +
                "\nFornitore: " + supplierName;

        /*// ✅ USA IL NUOVO METODO della boundary invece di creare NotificationBean direttamente
        notificationBoundary.addNotification(
                confirmationMsg,
                order,                              // relatedOrder
                java.time.LocalDate.now().toString(), // date
                null                                // partName
        );



        new OrderProcessorThread(order).start();
        return order;

         */

        NotificationBean confirmationNotif = new NotificationBean(
                confirmationMsg,
                order,
                java.time.LocalDate.now().toString(),
                null
        );
        notificationBoundary.addNotification(confirmationNotif);

        new OrderProcessorThread(order).start();
        return order;
    }

    /** Crea un ordine dai suggerimenti delle notifiche usando il fornitore scelto */
    public OrderBean createSuggestedOrder(String username, List<OrderItemBean> suggestedItems, String supplierName) {
        if (supplierName == null || supplierName.isBlank()) {
            return null;
        }
        return createOrder(username, supplierName, suggestedItems);
    }

    /** Recupera tutti gli ordini */
    public List<OrderBean> getAllOrders() {
        return orderManager.getAllOrders();
    }

    /** Recupera tutti i nomi dei fornitori */
    public List<String> getSupplierNames() {
        return supplierManager.getAllSuppliers()
                .stream()
                .map(s -> s.getName())
                .toList();
    }

    private String generateOrderID() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}