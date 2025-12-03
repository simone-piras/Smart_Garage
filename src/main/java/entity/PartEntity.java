package entity;

public class PartEntity {
    private int id;
    private String name;
    private int quantity;
    private int threshold;
    private String ownerUsername;

    // Costruttore senza ID (per nuovi oggetti)
    public PartEntity(String name, int quantity, int threshold, String ownerUsername) {
        this(0, name, quantity, threshold, ownerUsername);
    }

    // Costruttore con ID (per oggetti esistenti)
    public PartEntity(int id, String name, int quantity, int threshold, String ownerUsername) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.threshold = threshold;
        this.ownerUsername = ownerUsername;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getThreshold() { return threshold; }
    public void setThreshold(int threshold) { this.threshold = threshold; }
    public int getReorderThreshold() { return getThreshold(); }


    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
}