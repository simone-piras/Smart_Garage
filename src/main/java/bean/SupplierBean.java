package bean;

import entity.SupplierEntity;

public class SupplierBean {

    private String name;
    private String email;
    private String phone;

    public SupplierBean() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void validate() {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Il nome del fornitore non può essere vuoto.");
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Email fornitore non valida.");
        if (phone == null || phone.isBlank())
            throw new IllegalArgumentException("Numero di telefono non può essere vuoto.");
    }

    public static SupplierBean fromEntity(SupplierEntity e) {
        SupplierBean b = new SupplierBean();
        b.setName(e.getName());
        b.setEmail(e.getEmail());
        b.setPhone(e.getPhone());
        return b;
    }
}

