package bean;

import entity.NotificationEntity;

public class NotificationBean {

    private String message;
    private OrderBean relatedOrder;
    private String date;
    private String partName;
    private boolean hasSuggestedOrder;
    private int suggestedQuantity;

    public NotificationBean() {}

    public NotificationBean(String message, OrderBean relatedOrder, String date, String partName) {
        this.message = message;
        this.relatedOrder = relatedOrder;
        this.date = date;
        this.partName = partName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OrderBean getRelatedOrder() {
        return relatedOrder;
    }

    public void setRelatedOrder(OrderBean relatedOrder) {
        this.relatedOrder = relatedOrder;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public boolean isHasSuggestedOrder() {
        return hasSuggestedOrder;
    }

    public void setHasSuggestedOrder(boolean hasSuggestedOrder) {
        this.hasSuggestedOrder = hasSuggestedOrder;
    }

    public int getSuggestedQuantity() {
        return suggestedQuantity;
    }

    public void setSuggestedQuantity(int suggestedQuantity) {
        this.suggestedQuantity = suggestedQuantity;
    }

    public void validate() {
        if (message == null || message.isBlank())
            throw new IllegalArgumentException("Il messaggio della notifica non può essere vuoto.");
        if (date == null || date.isBlank())
            throw new IllegalArgumentException("La data non può essere vuota.");
    }

    public static NotificationBean fromEntity(NotificationEntity e) {
        NotificationBean b = new NotificationBean();
        b.setMessage(e.getMessage());
        b.setDate(e.getDate());
        b.setPartName(e.getPartName());
        b.setHasSuggestedOrder(e.isHasSuggestedOrder());
        b.setSuggestedQuantity(e.getSuggestedQuantity());
        if (e.getRelatedOrder() != null)
            b.setRelatedOrder(OrderBean.fromEntity(e.getRelatedOrder()));
        return b;
    }
}
