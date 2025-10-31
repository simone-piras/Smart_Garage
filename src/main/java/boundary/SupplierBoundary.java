package boundary;

import bean.SupplierBean;
import bean.UserBean;
import controller.SupplierManager;
import controller.UserManager;

import java.util.List;

public class SupplierBoundary {
    private final SupplierManager supplierManager = new SupplierManager();
    private final UserManager userManager = new UserManager();

    public List<SupplierBean> getAllSuppliers() {
        return supplierManager.getAllSuppliers();
    }

    public boolean setDefaultSupplier(String username, String supplierName) {
        UserBean user = userManager.getUser(username);
        if (user == null) return false;
        userManager.setDefaultSupplier(username, supplierName);
        user.setDefaultSupplierName(supplierName);
        return true;
    }

    // ✅ NUOVO METODO per aggiungere supplier con parametri semplici
    public void addSupplier(String name, String email, String phone) {
        SupplierBean supplier = new SupplierBean();
        supplier.setName(name);
        supplier.setEmail(email);
        supplier.setPhone(phone);

        // ✅ AGGIUNGI validate()
        supplier.validate();
        supplierManager.addSupplier(supplier);
    }
}