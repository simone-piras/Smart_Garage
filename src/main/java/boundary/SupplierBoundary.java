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
        userManager.setDefaultSupplier(username, supplierName); //salva fornitore predefinito
        user.setDefaultSupplierName(supplierName); //aggiorna oggetto in memoria
        return true;
    }


    public void addSupplier(String name, String email, String phone) {
        SupplierBean supplier = new SupplierBean(); //crea oggetto supplier
        supplier.setName(name);
        supplier.setEmail(email);
        supplier.setPhone(phone);
        supplier.validate();
        supplierManager.addSupplier(supplier);
    }
}