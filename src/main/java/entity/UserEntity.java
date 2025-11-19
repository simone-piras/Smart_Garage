package entity;

public class UserEntity {
    private int id;
    private String username;
    private String email;
    private String password;
    private String defaultSupplierName;

    // Costruttore senza ID per nuovi utenti
    public UserEntity(String username, String email, String password, String defaultSupplierName) {
        this(0, username, email, password, defaultSupplierName);
    }

    // Costruttore con ID per utenti già esistenti
    public UserEntity(int id, String username, String email, String password, String defaultSupplierName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.defaultSupplierName = defaultSupplierName;
    }

    // Getter e Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDefaultSupplierName() { return defaultSupplierName; }
    public void setDefaultSupplierName(String defaultSupplierName) { this.defaultSupplierName = defaultSupplierName; }
}
