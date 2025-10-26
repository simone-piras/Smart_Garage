package DatabaseDAO;

import DAO.OrderDAO;
import entity.OrderEntity;
import entity.SupplierEntity;
import entity.OrderItemEntity;
import entity.PartEntity;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseOrderDAO implements OrderDAO {

    @Override
    public void saveOrder(OrderEntity order) {
        String sql = "INSERT INTO orders (id, supplier_name, status) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, order.getId());
            ps.setString(2, order.getSupplierName());
            ps.setString(3, order.getStatus());
            ps.executeUpdate();

            saveOrderItems(order.getId(), order.getItems(), conn);

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel salvataggio ordine: " + e.getMessage(), e);
        }
    }

    private void saveOrderItems(String orderId, List<OrderItemEntity> items, Connection conn) throws SQLException {
        if (items == null || items.isEmpty()) return;

        String sql = "INSERT INTO order_items (order_id, part_name, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (OrderItemEntity item : items) {
                ps.setString(1, orderId);
                ps.setString(2, item.getPartName());
                ps.setInt(3, item.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public Optional<OrderEntity> getOrderByID(String orderID) {
        // ✅ MODIFICA: Rimuovi 'date' dalla query se non esiste nel DB
        String sql = "SELECT * FROM orders WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                SupplierEntity supplier = null;
                String supplierName = rs.getString("supplier_name");
                if (supplierName != null) {
                    supplier = new SupplierEntity(supplierName, null, null, false);
                }

                List<OrderItemEntity> items = getOrderItems(orderID, conn);

                OrderEntity order = new OrderEntity(
                        supplier,
                        items,
                        rs.getString("status"),
                        null // ✅ Imposta date a null se non esiste nel DB
                );
                order.setId(rs.getString("id"));

                return Optional.of(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero ordine: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private List<OrderItemEntity> getOrderItems(String orderId, Connection conn) throws SQLException {
        List<OrderItemEntity> items = new ArrayList<>();
        String sql = "SELECT part_name, quantity FROM order_items WHERE order_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String partName = rs.getString("part_name");
                int quantity = rs.getInt("quantity");

                PartEntity part = new PartEntity(partName, 0, 0);
                OrderItemEntity item = new OrderItemEntity(part, quantity, null);
                items.add(item);
            }
        }
        return items;
    }

    @Override
    public List<OrderEntity> getAllOrders() {
        List<OrderEntity> orders = new ArrayList<>();
        String sql = "SELECT id FROM orders";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                getOrderByID(rs.getString("id"))
                        .ifPresent(orders::add);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero ordini: " + e.getMessage(), e);
        }
        return orders;
    }

    @Override
    public boolean deleteOrder(String orderID) {
        try {
            // Prima elimina gli order items
            String deleteItemsSQL = "DELETE FROM order_items WHERE order_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(deleteItemsSQL)) {
                ps.setString(1, orderID);
                ps.executeUpdate();
            }

            // Poi elimina l'ordine
            String deleteOrderSQL = "DELETE FROM orders WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(deleteOrderSQL)) {
                ps.setString(1, orderID);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'eliminazione ordine: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateOrder(OrderEntity updatedOrder) {
        // Per semplicità: elimina e ricrea
        deleteOrder(updatedOrder.getId());
        saveOrder(updatedOrder);
    }
}