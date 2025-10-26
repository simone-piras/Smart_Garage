package utils;
import DAO.*;
import enumerations.PersistenceType;

public abstract class DAOFactory {
    public abstract UserDAO getUserDAO();
    public abstract InventoryDAO getInventoryDAO();
    public abstract OrderDAO getOrderDAO();
    public abstract NotificationDAO getNotificationDAO();
    public abstract SupplierDAO getSupplierDAO();

    public static DAOFactory getDAOFactory(PersistenceType type) {
        return switch (type) {
            case IN_MEMORY -> new InMemoryDAOFactory();
            case DATABASE -> new DatabaseDAOFactory();
            case FILE -> new FileDAOFactory();
        };
    }
}
