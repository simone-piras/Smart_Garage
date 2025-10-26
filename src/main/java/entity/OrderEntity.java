package entity;

import java.util.List;

public class OrderEntity {
    private String id;
    private SupplierEntity supplier;
    private List<OrderItemEntity> items;
    private String status;
    private String date;

    // Costruttore senza ID
    public OrderEntity(SupplierEntity supplier, List<OrderItemEntity> items, String status, String date) {
        this(null, supplier, items, status, date);
    }

    // Costruttore con ID
    public OrderEntity(String id, SupplierEntity supplier, List<OrderItemEntity> items, String status, String date) {
        this.id = id;
        this.supplier = supplier;
        this.items = items;
        this.status = status;
        this.date = date;
    }

    // Getter e Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public SupplierEntity getSupplier() { return supplier; }
    public void setSupplier(SupplierEntity supplier) { this.supplier = supplier; }

    public List<OrderItemEntity> getItems() { return items; }
    public void setItems(List<OrderItemEntity> items) { this.items = items; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    // Metodi di compatibilità
    public String getSupplierName() { return supplier != null ? supplier.getName() : null; }
    public String getOrderID() { return id; }
}
