package InMemoryDAO;

import DAO.OrderDAO;
import entity.OrderEntity;
import utils.SessionManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryOrderDAO implements OrderDAO {
    private final List<OrderEntity> orders = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    private String getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser().getUsername();
    }

    @Override
    public void saveOrder(OrderEntity order) {
        if (order.getId() == null || order.getId().isEmpty()) {
            // Genera ID
            String newId = "order_" + nextId.getAndIncrement();
            // Crea nuovo ordine associato all'utente corrente
            OrderEntity newOrder = new OrderEntity(
                    newId,
                    order.getSupplier(),
                    order.getItems(),
                    order.getStatus(),
                    order.getDate(),
                    getCurrentUser() // Proprietario
            );
            orders.add(newOrder);
        } else {
            // Update esistente: rimuovi vecchio (se mio) e aggiungi nuovo
            deleteOrder(order.getId());

            // Assicurati che l'oggetto abbia l'owner corretto
            order.setOwnerUsername(getCurrentUser());
            orders.add(order);
        }
    }

    @Override
    public Optional<OrderEntity> getOrderByID(String orderID) {
        return orders.stream()
                .filter(o -> o.getId().equals(orderID))
                .filter(o -> o.getOwnerUsername().equals(getCurrentUser())) // Sicurezza
                .findFirst();
    }

    @Override
    public List<OrderEntity> getAllOrders() {
        return orders.stream()
                .filter(o -> o.getOwnerUsername().equals(getCurrentUser())) // Solo i miei ordini
                .toList();
    }

    @Override
    public boolean deleteOrder(String orderID) {
        return orders.removeIf(order ->
                order.getId().equals(orderID) &&
                        order.getOwnerUsername().equals(getCurrentUser()));
    }

    @Override
    public void updateOrder(OrderEntity updatedOrder) {
        // Riutilizziamo la logica di saveOrder che gestisce già l'aggiornamento
        saveOrder(updatedOrder);
    }
}