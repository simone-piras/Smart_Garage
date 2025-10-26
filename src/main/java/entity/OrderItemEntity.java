package entity;

import java.math.BigDecimal;

public class OrderItemEntity {
    private int id;
    private PartEntity part;
    private int quantity;
    private BigDecimal unitPrice;

    // Costruttore senza ID
    public OrderItemEntity(PartEntity part, int quantity, BigDecimal unitPrice) {
        this(0, part, quantity, unitPrice);
    }

    // Costruttore con ID
    public OrderItemEntity(int id, PartEntity part, int quantity, BigDecimal unitPrice) {
        this.id = id;
        this.part = part;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getter e Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public PartEntity getPart() { return part; }
    public void setPart(PartEntity part) { this.part = part; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    // Metodi di compatibilità
    public String getPartName() { return part != null ? part.getName() : null; }
}
