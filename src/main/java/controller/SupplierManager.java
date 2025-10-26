package controller;

import bean.SupplierBean;
import DAO.SupplierDAO;
import entity.SupplierEntity;
import mapper.BeanEntityMapperFactory;
import utils.ApplicationContext;

import java.util.List;
import java.util.Optional;

public class SupplierManager {
    private final SupplierDAO supplierDAO;
    private final BeanEntityMapperFactory mapperFactory = BeanEntityMapperFactory.getInstance();

    public SupplierManager() {
        this.supplierDAO = ApplicationContext.getInstance().getDAOFactory().getSupplierDAO();
    }

    public void addSupplier(SupplierBean supplierBean) {
        SupplierEntity entity = mapperFactory.toEntity(supplierBean, SupplierEntity.class);
        supplierDAO.saveSupplier(entity);
    }

    public Optional<SupplierBean> getSupplierByName(String name) {
        return supplierDAO.getSupplierByName(name)
                .map(entity -> mapperFactory.toBean(entity, SupplierBean.class));
    }

    public List<SupplierBean> getAllSuppliers() {
        return supplierDAO.getAllSuppliers().stream()
                .map(entity -> mapperFactory.toBean(entity, SupplierBean.class))
                .toList();
    }
}
