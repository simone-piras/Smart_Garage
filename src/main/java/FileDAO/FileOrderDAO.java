package FileDAO;

import DAO.OrderDAO;
import entity.OrderEntity;
import entity.OrderItemEntity;
import entity.SupplierEntity;
import entity.PartEntity;
import java.io.*;
import java.util.*;

public class FileOrderDAO implements OrderDAO {

    private static final String FILE_PATH = "data/orders.txt";

    @Override
    public void saveOrder(OrderEntity order) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(formatOrder(order));
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Errore scrittura ordine su file: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<OrderEntity> getOrderByID(String orderID) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                OrderEntity order = parseOrder(line);
                if (order.getId().equals(orderID)) return Optional.of(order); // ✅ Confronta String direttamente
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore lettura ordine da file: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<OrderEntity> getAllOrders() {
        List<OrderEntity> orders = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) orders.add(parseOrder(line));
        } catch (IOException e) {
            throw new RuntimeException("Errore lettura ordini da file: " + e.getMessage(), e);
        }
        return orders;
    }

    @Override
    public boolean deleteOrder(String orderID) {
        List<OrderEntity> orders = getAllOrders();
        boolean removed = orders.removeIf(o -> o.getId().equals(orderID)); // ✅ Confronta String direttamente
        if (removed) rewriteAll(orders);
        return removed;
    }

    @Override
    public void updateOrder(OrderEntity updatedOrder) {
        List<OrderEntity> orders = getAllOrders();
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getId().equals(updatedOrder.getId())) { // ✅ Confronta String
                orders.set(i, updatedOrder);
                rewriteAll(orders);
                return;
            }
        }
    }

    private String formatOrder(OrderEntity order) {
        StringBuilder sb = new StringBuilder();
        sb.append(order.getId()).append("|");
        sb.append(order.getSupplierName() == null ? "" : order.getSupplierName()).append("|");
        sb.append(order.getStatus()).append("|");
        sb.append(order.getDate()).append("|");

        // Formatta items
        for (int i = 0; i < order.getItems().size(); i++) {
            OrderItemEntity item = order.getItems().get(i);
            sb.append(item.getPartName()).append(":").append(item.getQuantity());
            if (i < order.getItems().size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    private OrderEntity parseOrder(String line) {
        String[] parts = line.split("\\|");
        String id = parts[0]; // ✅ Ora è String
        String supplierName = parts[1].isEmpty() ? null : parts[1];
        String status = parts[2];
        String date = parts[3];

        List<OrderItemEntity> items = new ArrayList<>();
        if (parts.length > 4 && !parts[4].isEmpty()) {
            String[] itemsStr = parts[4].split(",");
            for (String itemStr : itemsStr) {
                String[] itemParts = itemStr.split(":");
                String partName = itemParts[0];
                int quantity = Integer.parseInt(itemParts[1]);

                // Crea OrderItemEntity con PartEntity fittizia
                OrderItemEntity item = new OrderItemEntity(
                        new PartEntity(partName, 0, 0),
                        quantity,
                        null
                );
                items.add(item);
            }
        }

        // Crea SupplierEntity fittizia se necessario
        SupplierEntity supplier = null;
        if (supplierName != null) {
            supplier = new SupplierEntity(supplierName, null, null, false);
        }

        return new OrderEntity(id, supplier, items, status, date); // ✅ Ora usa String id
    }

    private void rewriteAll(List<OrderEntity> orders) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (OrderEntity order : orders) {
                writer.write(formatOrder(order));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore riscrittura file ordini: " + e.getMessage(), e);
        }
    }
}
