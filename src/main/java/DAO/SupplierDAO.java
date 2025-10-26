package DAO;

import entity.SupplierEntity;
import java.util.*;

public interface SupplierDAO {
    void saveSupplier(SupplierEntity supplier);
    Optional<SupplierEntity> getSupplierByName(String name);
    List<SupplierEntity> getAllSuppliers();
}