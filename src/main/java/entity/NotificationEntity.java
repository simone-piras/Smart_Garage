package entity;

public class NotificationEntity {
    private int id;
    private String message;
    private String date;
    private String partName;
    private boolean hasSuggestedOrder;
    private int suggestedQuantity;
    private OrderEntity relatedOrder;
    private String ownerUsername;

    // Costruttore senza ID
    public NotificationEntity(String message, String date, String partName,
                              boolean hasSuggestedOrder, int suggestedQuantity, OrderEntity relatedOrder, String ownerUsername) {
        this(0, message, date, partName, hasSuggestedOrder, suggestedQuantity, relatedOrder, ownerUsername);
    }

    // Costruttore con ID
    @SuppressWarnings("java:S107")
    public NotificationEntity(int id, String message, String date, String partName,
                              boolean hasSuggestedOrder, int suggestedQuantity, OrderEntity relatedOrder, String ownerUsername) {
        this.id = id;
        this.message = message;
        this.date = date;
        this.partName = partName;
        this.hasSuggestedOrder = hasSuggestedOrder;
        this.suggestedQuantity = suggestedQuantity;
        this.relatedOrder = relatedOrder;
        this.ownerUsername = ownerUsername;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
    public boolean isHasSuggestedOrder() { return hasSuggestedOrder; }
    public void setHasSuggestedOrder(boolean hasSuggestedOrder) { this.hasSuggestedOrder = hasSuggestedOrder; }
    public int getSuggestedQuantity() { return suggestedQuantity; }
    public void setSuggestedQuantity(int suggestedQuantity) { this.suggestedQuantity = suggestedQuantity; }
    public OrderEntity getRelatedOrder() { return relatedOrder; }
    public void setRelatedOrder(OrderEntity relatedOrder) { this.relatedOrder = relatedOrder; }
    public boolean isRead() { return false; }
    public boolean hasSuggestedOrder() { return isHasSuggestedOrder(); }


    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
}
