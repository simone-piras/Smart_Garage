package InMemoryDAO;

import DAO.SupplierDAO;
import entity.SupplierEntity;
import java.util.*;

public class InMemorySupplierDAO implements SupplierDAO {
    private final List<SupplierEntity> suppliers = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void saveSupplier(SupplierEntity supplier) {
        if (supplier.getId() == 0) {
            SupplierEntity newSupplier = new SupplierEntity(nextId++, supplier.getName(),
                    supplier.getEmail(), supplier.getPhoneNumber(), supplier.isDefault());
            suppliers.add(newSupplier);
        } else {
            // Se ha ID, rimuovi eventuale duplicato e aggiungi
            suppliers.removeIf(s -> s.getId() == supplier.getId());
            suppliers.add(supplier);
        }
    }

    @Override
    public Optional<SupplierEntity> getSupplierByName(String name) {
        return suppliers.stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<SupplierEntity> getAllSuppliers() {
        return new ArrayList<>(suppliers);
    }
}