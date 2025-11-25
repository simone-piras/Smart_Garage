package entity;

public class SupplierEntity {
    private int id;
    private String name;
    private String email;
    private String phoneNumber;
    private boolean isDefault;

    // Costruttore senza ID per nuovi fornitori
    public SupplierEntity(String name, String email, String phoneNumber, boolean isDefault) {
        this(0, name, email, phoneNumber, isDefault);
    }

    // Costruttore con ID per fornitori esistenti
    public SupplierEntity(int id, String name, String email, String phoneNumber, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.isDefault = isDefault;
    }

    // Getter e Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    // Metodi di compatibilità
    public String getPhone() { return getPhoneNumber(); }
}
