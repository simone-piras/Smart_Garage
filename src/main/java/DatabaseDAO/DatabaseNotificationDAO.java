package DatabaseDAO;

import DAO.NotificationDAO;
import entity.NotificationEntity;
import utils.DBConnection;
import exception.DatabaseOperationException;
import utils.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseNotificationDAO implements NotificationDAO {

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_MESSAGE = "message";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_PART_NAME = "part_name";
    private static final String COLUMN_HAS_SUGGESTED_ORDER = "has_suggested_order";
    private static final String COLUMN_SUGGESTED_QUANTITY = "suggested_quantity";

    private String getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser().getUsername();
    }

    @Override
    public void saveNotification(NotificationEntity notification) {
        String sql = "INSERT INTO notifications (message, date, part_name, has_suggested_order, suggested_quantity, user_username) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, notification.getMessage());
            ps.setString(2, notification.getDate());
            ps.setString(3, notification.getPartName());
            ps.setBoolean(4, notification.isHasSuggestedOrder());
            ps.setInt(5, notification.getSuggestedQuantity());
            ps.setString(6, getCurrentUser()); // Proprietario

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    notification.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Errore nel salvataggio notifica: " + e.getMessage(), e);
        }
    }

    @Override
    public List<NotificationEntity> getAllNotifications() {
        List<NotificationEntity> notifications = new ArrayList<>();
        // Filtriamo per utente
        String sql = "SELECT id, message, date, part_name, has_suggested_order, suggested_quantity FROM notifications WHERE user_username = ? ORDER BY date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, getCurrentUser());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                NotificationEntity notification = new NotificationEntity(
                        rs.getString(COLUMN_MESSAGE),
                        rs.getString(COLUMN_DATE),
                        rs.getString(COLUMN_PART_NAME),
                        rs.getBoolean(COLUMN_HAS_SUGGESTED_ORDER),
                        rs.getInt(COLUMN_SUGGESTED_QUANTITY),
                        null,
                        getCurrentUser() // Proprietario
                );
                notification.setId(rs.getInt(COLUMN_ID));

                notifications.add(notification);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Errore nel recupero notifiche: " + e.getMessage(), e);
        }
        return notifications;
    }

    @Override
    public void clearNotifications() {
        // Cancella solo le mie notifiche
        String sql = "DELETE FROM notifications WHERE user_username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, getCurrentUser());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Errore durante la pulizia notifiche: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeNotification(NotificationEntity notification) {
        // Cancella solo se è mia e ha quell'ID
        String sql = "DELETE FROM notifications WHERE id = ? AND user_username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notification.getId());
            ps.setString(2, getCurrentUser());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Errore nell'eliminazione notifica: " + e.getMessage(), e);
        }
    }
}