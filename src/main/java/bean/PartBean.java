package bean;

import entity.PartEntity;

public class PartBean {

    private String name;
    private int quantity;
    private int reorderThreshold;

    public PartBean() {}

    public PartBean(String name, int quantity, int reorderThreshold) {
        this.name = name;
        this.quantity = quantity;
        this.reorderThreshold = reorderThreshold;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0)
            throw new IllegalArgumentException("La quantità non può essere negativa.");
        this.quantity = quantity;
    }

    public int getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(int reorderThreshold) {
        if (reorderThreshold < 0)
            throw new IllegalArgumentException("La soglia di riordino non può essere negativa.");
        this.reorderThreshold = reorderThreshold;
    }

    public void validate() {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Il nome del pezzo non può essere vuoto.");
    }

    public static PartBean fromEntity(PartEntity entity) {
        PartBean bean = new PartBean();
        bean.setName(entity.getName());
        bean.setQuantity(entity.getQuantity());
        bean.setReorderThreshold(entity.getReorderThreshold());
        return bean;
    }
}
