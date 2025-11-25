package bean;

import entity.OrderEntity;
import enumerations.OrderStatus;
import java.util.List;


public class OrderBean {

    private String orderID;
    private String supplierName;
    private OrderStatus status;
    private List<OrderItemBean> items;


    public OrderBean() {
        /*
     Costruttore vuoto utilizzato dai BeanEntityMapper per la conversione
     da Entity a Bean. Necessario per permettere la creazione graduale
     dell'oggetto con controlli null e logica condizionale complessa
     durante il mapping.
     */
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderItemBean> getItems() {
        return items;
    }

    public void setItems(List<OrderItemBean> items) {
        this.items = items;
    }

    public void validate() {
        if (orderID == null || orderID.isBlank())
            throw new IllegalArgumentException("ID ordine non può essere vuoto.");
        if (supplierName == null || supplierName.isBlank())
            throw new IllegalArgumentException("Nome fornitore non può essere vuoto.");
        if (status == null)
            throw new IllegalArgumentException("Stato ordine non può essere nullo.");
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("Un ordine deve contenere almeno un articolo.");
    }

    public static OrderBean fromEntity(OrderEntity e) {
        OrderBean b = new OrderBean();
        b.setOrderID(e.getOrderID());
        b.setSupplierName(e.getSupplierName());


        if (e.getStatus() != null) {
            try {
                // Converte "In process" → "IN_PROCESS" → OrderStatus.IN_PROCESS
                String statusUpper = e.getStatus().toUpperCase().replace(" ", "_");
                OrderStatus orderStatus = OrderStatus.valueOf(statusUpper);
                b.setStatus(orderStatus);
            } catch (IllegalArgumentException _) {
                // Se la conversione fallisce, usa stato di default
                b.setStatus(OrderStatus.CREATING);
            }
        } else {
            b.setStatus(OrderStatus.CREATING);
        }

        if (e.getItems() != null)
            b.setItems(e.getItems().stream()
                    .map(OrderItemBean::fromEntity)
                    .toList());
        return b;
    }
}
