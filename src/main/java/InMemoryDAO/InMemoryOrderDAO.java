package InMemoryDAO;

import DAO.OrderDAO;
import entity.OrderEntity;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryOrderDAO implements OrderDAO {
    private final List<OrderEntity> orders = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @Override
    public void saveOrder(OrderEntity order) {
        if (order.getId() == null || order.getId().isEmpty()) {
            // ✅ Genera ID come stringa (simula UUID)
            String newId = "order_" + nextId.getAndIncrement();
            OrderEntity newOrder = new OrderEntity(newId, order.getSupplier(), order.getItems(),
                    order.getStatus(), order.getDate());
            orders.add(newOrder);
        } else {
            // Se ha già ID, controlla se esiste già
            boolean exists = orders.stream().anyMatch(o -> o.getId().equals(order.getId()));
            if (!exists) {
                orders.add(order);
            }
        }
    }

    @Override
    public Optional<OrderEntity> getOrderByID(String orderID) {
        return orders.stream()
                .filter(order -> order.getId().equals(orderID)) // ✅ Confronta String
                .findFirst();
    }

    @Override
    public List<OrderEntity> getAllOrders() {
        return new ArrayList<>(orders);
    }

    @Override
    public boolean deleteOrder(String orderID) {
        return orders.removeIf(order -> order.getId().equals(orderID)); // ✅ Confronta String
    }

    @Override
    public void updateOrder(OrderEntity updatedOrder) {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getId().equals(updatedOrder.getId())) { // ✅ Confronta String
                orders.set(i, updatedOrder);
                return;
            }
        }
        // Se non trovato, salva come nuovo
        saveOrder(updatedOrder);
    }
}
