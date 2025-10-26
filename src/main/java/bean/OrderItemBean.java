package bean;

import entity.OrderItemEntity;

public class OrderItemBean {

    private String partName;
    private int quantity;

    public OrderItemBean() {}

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("La quantità deve essere maggiore di zero.");
        this.quantity = quantity;
    }

    public void validate() {
        if (partName == null || partName.isBlank())
            throw new IllegalArgumentException("Il nome del pezzo non può essere vuoto.");
    }

    public static OrderItemBean fromEntity(OrderItemEntity e) {
        OrderItemBean b = new OrderItemBean();
        b.setPartName(e.getPartName());
        b.setQuantity(e.getQuantity());
        return b;
    }
}
