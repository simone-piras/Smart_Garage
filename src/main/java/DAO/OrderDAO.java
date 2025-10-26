package DAO;

import entity.OrderEntity;
import java.util.List;
import java.util.Optional;

public interface OrderDAO {
    void saveOrder(OrderEntity order);
    Optional<OrderEntity> getOrderByID(String orderID);
    List<OrderEntity> getAllOrders();
    boolean deleteOrder(String orderID);
    void updateOrder(OrderEntity updatedOrder);
}
