package utils;
import DAO.*;
import FileDAO.*;

public class FileDAOFactory extends DAOFactory {
    private final NotificationDAO notificationDAO = new FileNotificationDAO();
    private final UserDAO userDAO = new FileUserDAO();
    private final InventoryDAO inventoryDAO = new FileInventoryDAO();
    private final OrderDAO orderDAO = new FileOrderDAO();
    private final SupplierDAO supplierDAO = new FileSuppliersDAO();

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