package DatabaseDAO;

import DAO.OrderDAO;
import entity.OrderEntity;
import entity.SupplierEntity;
import entity.OrderItemEntity;
import entity.PartEntity;
import utils.DBConnection;
import exception.DatabaseOperationException;
import utils.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseOrderDAO implements OrderDAO {

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_SUPPLIER_NAME = "supplier_name";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_PART_NAME = "part_name";
    private static final String COLUMN_QUANTITY = "quantity";

    private String getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser().getUsername();
    }

    @Override
    public void saveOrder(OrderEntity order) {
        String sql = "INSERT INTO orders (id, supplier_name, status, user_username) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, order.getId());
            ps.setString(2, order.getSupplierName());
            ps.setString(3, order.getStatus());
            ps.setString(4, getCurrentUser()); // Proprietario
            ps.executeUpdate();

            // Gli item non hanno bisogno dell'utente esplicitamente perché sono legati all'ID dell'ordine
            saveOrderItems(order.getId(), order.getItems(), conn);

        } catch (SQLException e) {
            throw new DatabaseOperationException("Errore nel salvataggio ordine: " + e.getMessage(), e);
        }
    }

    private void saveOrderItems(String orderId, List<OrderItemEntity> items, Connection conn) throws SQLException {
        if (items == null || items.isEmpty()) return;

        String sql = "INSERT INTO order_items (order_id, part_name, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);

            for (OrderItemEntity item : items) {
                ps.setString(2, item.getPartName());
                ps.setInt(3, item.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public Optional<OrderEntity> getOrderByID(String orderID) {
        // Filtriamo per ID ordine E utente
        String sql = "SELECT id, supplier_name, status FROM orders WHERE id = ? AND user_username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderID);
            ps.setString(2, getCurrentUser());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                SupplierEntity supplier = null;
                String supplierName = rs.getString(COLUMN_SUPPLIER_NAME);
                if (supplierName != null) {
                    supplier = new SupplierEntity(supplierName, null, null, false);
                }

                List<OrderItemEntity> items = getOrderItems(orderID, conn);

                OrderEntity order = new OrderEntity(
                        supplier,
                        items,
                        rs.getString(COLUMN_STATUS),
                        null,
                        getCurrentUser() // Proprietario
                );
                order.setId(rs.getString(COLUMN_ID));

                return Optional.of(order);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Errore nel recupero ordine: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private List<OrderItemEntity> getOrderItems(String orderId, Connection conn) throws SQLException {
        List<OrderItemEntity> items = new ArrayList<>();
        // Qui non serve filtrare per user, perché stiamo già filtrando per order_id che è univoco
        String sql = "SELECT part_name, quantity FROM order_items WHERE order_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String partName = rs.getString(COLUMN_PART_NAME);
                int quantity = rs.getInt(COLUMN_QUANTITY);


                PartEntity part = new PartEntity(partName, 0, 0, getCurrentUser());
                OrderItemEntity item = new OrderItemEntity(part, quantity, null);
                items.add(item);
            }
        }
        return items;
    }

    @Override
    public List<OrderEntity> getAllOrders() {
        List<OrderEntity> orders = new ArrayList<>();
        // SOLO GLI ORDINI DELL'UTENTE
        String sql = "SELECT id FROM orders WHERE user_username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, getCurrentUser());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                getOrderByID(rs.getString(COLUMN_ID))
                        .ifPresent(orders::add);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Errore nel recupero ordini: " + e.getMessage(), e);
        }
        return orders;
    }

    @Override
    public boolean deleteOrder(String orderID) {
        try {

            String deleteItemsSQL = "DELETE FROM order_items WHERE order_id = ? AND order_id IN (SELECT id FROM orders WHERE user_username = ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(deleteItemsSQL)) {
                ps.setString(1, orderID);
                ps.setString(2, getCurrentUser());
                ps.executeUpdate();
            }

            // Elimina l'ordine solo se appartiene all'utente
            String deleteOrderSQL = "DELETE FROM orders WHERE id = ? AND user_username = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(deleteOrderSQL)) {
                ps.setString(1, orderID);
                ps.setString(2, getCurrentUser());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Errore nell'eliminazione ordine: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateOrder(OrderEntity updatedOrder) {
        // Elimina (se mio) e ricrea
        deleteOrder(updatedOrder.getId());
        saveOrder(updatedOrder);
    }
}