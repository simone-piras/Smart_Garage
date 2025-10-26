package DatabaseDAO;

import DAO.NotificationDAO;
import entity.NotificationEntity;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseNotificationDAO implements NotificationDAO {

    @Override
    public void saveNotification(NotificationEntity notification) {
        String sql = "INSERT INTO notifications (message, date, part_name, has_suggested_order, suggested_quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, notification.getMessage());
            ps.setString(2, notification.getDate());
            ps.setString(3, notification.getPartName());
            ps.setBoolean(4, notification.isHasSuggestedOrder());
            ps.setInt(5, notification.getSuggestedQuantity());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    notification.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel salvataggio notifica: " + e.getMessage(), e);
        }
    }

    @Override
    public List<NotificationEntity> getAllNotifications() {
        List<NotificationEntity> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY date DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                NotificationEntity notification = new NotificationEntity(
                        rs.getString("message"),
                        rs.getString("date"),
                        rs.getString("part_name"),
                        rs.getBoolean("has_suggested_order"),
                        rs.getInt("suggested_quantity"),
                        null
                );
                notification.setId(rs.getInt("id"));

                notifications.add(notification);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero notifiche: " + e.getMessage(), e);
        }
        return notifications;
    }

    @Override
    public void clearNotifications() {
        String sql = "DELETE FROM notifications";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la pulizia notifiche: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeNotification(NotificationEntity notification) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notification.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'eliminazione notifica: " + e.getMessage(), e);
        }
    }
}