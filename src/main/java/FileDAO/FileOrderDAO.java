package FileDAO;

import DAO.OrderDAO;
import entity.OrderEntity;
import entity.OrderItemEntity;
import entity.SupplierEntity;
import entity.PartEntity;
import exception.FilePersistenceException;
import utils.SessionManager;

import java.io.*;
import java.util.*;

public class FileOrderDAO implements OrderDAO {

    private static final String FILE_PATH = "data/orders.txt";

    private String getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser().getUsername();
    }

    // Metodo per caricare TUTTI gli ordini (anche di altri utenti)
    private List<OrderEntity> loadAllRaw() {
        List<OrderEntity> orders = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return orders;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if(!line.trim().isEmpty()) {
                    orders.add(parseOrder(line));
                }
            }
        } catch (IOException e) {
            throw new FilePersistenceException("Errore lettura ordini da file: " + e.getMessage(), e);
        }
        return orders;
    }

    @Override
    public void saveOrder(OrderEntity order) {
        // Imposto il proprietario
        order.setOwnerUsername(getCurrentUser());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(formatOrder(order));
            writer.newLine();
        } catch (IOException e) {
            throw new FilePersistenceException("Errore scrittura ordine su file: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<OrderEntity> getOrderByID(String orderID) {
        return loadAllRaw().stream()
                .filter(o -> o.getId().equals(orderID))
                .filter(o -> o.getOwnerUsername().equals(getCurrentUser())) // Controllo proprietario
                .findFirst();
    }

    @Override
    public List<OrderEntity> getAllOrders() {
        // Restituisco solo i MIEI ordini
        return loadAllRaw().stream()
                .filter(o -> o.getOwnerUsername().equals(getCurrentUser()))
                .toList();
    }

    @Override
    public boolean deleteOrder(String orderID) {
        List<OrderEntity> allOrders = loadAllRaw();
        // Rimuovo solo se ID coincide E sono il proprietario
        boolean removed = allOrders.removeIf(o ->
                o.getId().equals(orderID) &&
                        o.getOwnerUsername().equals(getCurrentUser()));

        if (removed) rewriteAll(allOrders);
        return removed;
    }

    @Override
    public void updateOrder(OrderEntity updatedOrder) {
        List<OrderEntity> allOrders = loadAllRaw();
        for (int i = 0; i < allOrders.size(); i++) {
            // Aggiorno solo se è il mio ordine
            if (allOrders.get(i).getId().equals(updatedOrder.getId()) &&
                    allOrders.get(i).getOwnerUsername().equals(getCurrentUser())) {

                updatedOrder.setOwnerUsername(getCurrentUser());
                allOrders.set(i, updatedOrder);
                rewriteAll(allOrders);
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

        // Items
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (int i = 0; i < order.getItems().size(); i++) {
                OrderItemEntity item = order.getItems().get(i);
                sb.append(item.getPartName()).append(":").append(item.getQuantity());
                if (i < order.getItems().size() - 1) sb.append(",");
            }
        } else {
            sb.append(" "); // Placeholder per items vuoti
        }

        // Aggiungo OWNER alla fine
        sb.append("|").append(order.getOwnerUsername());

        return sb.toString();
    }

    private OrderEntity parseOrder(String line) {
        String[] parts = line.split("\\|");
        String id = parts[0];
        String supplierName = parts[1].isEmpty() ? null : parts[1];
        String status = parts[2];
        String date = parts[3];

        List<OrderItemEntity> items = new ArrayList<>();
        if (parts.length > 4 && !parts[4].trim().isEmpty()) {
            String[] itemsStr = parts[4].split(",");
            for (String itemStr : itemsStr) {
                String[] itemParts = itemStr.split(":");
                if (itemParts.length >= 2) {
                    String partName = itemParts[0];
                    int quantity = Integer.parseInt(itemParts[1]);
                    // PartEntity fittizia per contenere il nome
                    OrderItemEntity item = new OrderItemEntity(
                            new PartEntity(partName, 0, 0, null),
                            quantity,
                            null
                    );
                    items.add(item);
                }
            }
        }

        // Recupero OWNER (gestione vecchi file)
        String owner = (parts.length > 5) ? parts[5] : getCurrentUser();

        SupplierEntity supplier = null;
        if (supplierName != null) {
            supplier = new SupplierEntity(supplierName, null, null, false);
        }

        return new OrderEntity(id, supplier, items, status, date, owner);
    }

    private void rewriteAll(List<OrderEntity> orders) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (OrderEntity order : orders) {
                writer.write(formatOrder(order));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new FilePersistenceException("Errore riscrittura file ordini: " + e.getMessage(), e);
        }
    }
}
