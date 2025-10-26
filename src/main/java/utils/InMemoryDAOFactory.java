package utils;
import DAO.*;
import InMemoryDAO.*;

public class InMemoryDAOFactory extends DAOFactory {

    private final UserDAO userDAO = new InMemoryUserDAO();
    private final InventoryDAO inventoryDAO = new InMemoryInventoryDAO();
    private final OrderDAO orderDAO = new InMemoryOrderDAO();
    private final NotificationDAO notificationDAO = new InMemoryNotificationDAO();
    private final SupplierDAO supplierDAO = new InMemorySupplierDAO();

    @Override
    public UserDAO getUserDAO() {
        return userDAO;
    }

    @Override
    public InventoryDAO getInventoryDAO() {
        return inventoryDAO;
    }

    @Override
    public OrderDAO getOrderDAO() {
        return orderDAO;
    }

    @Override
    public NotificationDAO getNotificationDAO() {
        return notificationDAO;
    }

    @Override
    public SupplierDAO getSupplierDAO() {
        return supplierDAO;
    }
}