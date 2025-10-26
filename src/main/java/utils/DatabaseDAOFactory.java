package utils;

import DAO.*;
import DatabaseDAO.*;

public class DatabaseDAOFactory extends DAOFactory {

    private final UserDAO userDAO = new DatabaseUserDAO();
    private final InventoryDAO inventoryDAO = new DatabaseInventoryDAO();
    private final OrderDAO orderDAO = new DatabaseOrderDAO();
    private final NotificationDAO notificationDAO = new DatabaseNotificationDAO();
    private final SupplierDAO supplierDAO = new DatabaseSupplierDAO();

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
