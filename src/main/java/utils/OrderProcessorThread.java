package utils;

import bean.NotificationBean;
import bean.OrderBean;
import bean.OrderItemBean;
import boundary.NotificationBoundary;
import enumerations.OrderStatus;
import enumerations.PersistenceType;
import controller.InventoryManager;
import controller.OrderManager;

import java.util.Optional;

public class OrderProcessorThread extends Thread {

    private final OrderBean order;
    private final OrderManager orderManager;
    private final InventoryManager inventoryManager;
    private final NotificationBoundary notificationBoundary;

    public OrderProcessorThread(OrderBean order) {
        this.order = order;
        this.orderManager = new OrderManager();
        this.inventoryManager = new InventoryManager();
        this.notificationBoundary = new NotificationBoundary(SharedManagers.getNotificationManager());
    }

    @Override
    public void run() {
        try {
            if (ApplicationContext.getInstance().getPersistenceType() != PersistenceType.DATABASE) {
                Thread.sleep(5000);
                order.setStatus(OrderStatus.IN_PROCESS);
                orderManager.updateOrder(order);

                Thread.sleep(5000);
                order.setStatus(OrderStatus.SHIPPED);
                orderManager.updateOrder(order);

                Thread.sleep(5000);
                order.setStatus(OrderStatus.DELIVERED);
                orderManager.updateOrder(order);
            } else {
                while (true) {
                    Optional<OrderBean> updated = orderManager.getOrderById(order.getOrderID());
                    if (updated.isPresent() && updated.get().getStatus() == OrderStatus.DELIVERED) {
                        break;
                    }
                    Thread.sleep(2000);
                }
            }

            // Se stato è DELIVERED → aggiorna inventario e notifiche
            Optional<OrderBean> finalOrder = orderManager.getOrderById(order.getOrderID());
            if (finalOrder.isPresent() && finalOrder.get().getStatus() == OrderStatus.DELIVERED) {
                OrderBean deliveredOrder = finalOrder.get();

                // Aggiorna inventario
                for (OrderItemBean item : deliveredOrder.getItems()) {
                    inventoryManager.addQuantityToPart(item.getPartName(), item.getQuantity());
                }

                // 🔹 Costruisci il riepilogo dettagliato
                StringBuilder riepilogo = new StringBuilder();
                int totaleArticoli = 0;

                for (OrderItemBean item : deliveredOrder.getItems()) {
                    riepilogo.append("  • ").append(item.getPartName())
                            .append(" - Quantità: ").append(item.getQuantity())
                            .append("\n");
                    totaleArticoli += item.getQuantity();
                }

                // 🔹 NOTIFICA CONSEGNA ORDINE CON RIEPILOGO DETTAGLIATO
                String deliveryMsg = "ORDINE CONSEGNATO #" + deliveredOrder.getOrderID() +
                        "\nData consegna: " +
                        java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                        "\nFornitore: " + deliveredOrder.getSupplierName() +
                        "\nTotale articoli: " + totaleArticoli +
                        "\n\nRiepilogo dettagliato:\n" + riepilogo.toString() +
                        "\nL'inventario è stato aggiornato automaticamente.";

                NotificationBean deliveryNotif = new NotificationBean(
                        deliveryMsg,
                        deliveredOrder,
                        java.time.LocalDateTime.now().toString(),
                        null // Nessuna parte specifica
                );
                notificationBoundary.addNotification(deliveryNotif);

                // ✅ MODIFICA: LE NOTIFICHE SCORTE BASSE SI CREANO AUTOMATICAMENTE
                // QUANDO inventoryManager.addQuantityToPart() CHIAMA checkThreshold()
                // → notifyObserver() → NotificationManager.update()
                // → addNotification() → SALVA NEL DB

                // ❌ ELIMINATO: notificationBoundary.refreshLowStockNotificationsForPart(item.getPartName());
                // ❌ ELIMINATO: new NotificationController(SharedManagers.getNotificationManager()).refreshLowStockNotifications();
            }

        } catch (InterruptedException e) {
            System.err.println("Errore nel thread ordine: " + e.getMessage());
        }
    }
}