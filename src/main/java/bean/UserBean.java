package bean;

import entity.UserEntity;

public class UserBean {

    private String username;
    private String password;
    private String email;
    private String defaultSupplierName;

    public UserBean() {}

    public UserBean(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDefaultSupplierName() {
        return defaultSupplierName;
    }

    public void setDefaultSupplierName(String defaultSupplierName) {
        this.defaultSupplierName = defaultSupplierName;
    }

    public void validate() {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username non può essere vuoto.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password non può essere vuota.");
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Email non valida.");
    }

    public static UserBean fromEntity(UserEntity e) {
        UserBean b = new UserBean();
        b.setUsername(e.getUsername());
        b.setPassword(e.getPassword());
        b.setEmail(e.getEmail());
        b.setDefaultSupplierName(e.getDefaultSupplierName());
        return b;
    }
}
