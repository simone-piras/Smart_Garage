package DatabaseDAO;

import DAO.InventoryDAO;
import entity.PartEntity;
import exception.DatabaseOperationException;
import utils.DBConnection;
import utils.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("java:S1068")
public class DatabaseInventoryDAO implements InventoryDAO {

    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_REORDER_THRESHOLD = "reorder_threshold";
    private static final String COLUMN_USER = "user_username";

    private static final String SQL_INSERT = "INSERT INTO parts (name, quantity, reorder_threshold, user_username) VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_BY_NAME = "SELECT name, quantity, reorder_threshold FROM parts WHERE name = ? AND user_username = ?";
    private static final String SQL_UPDATE = "UPDATE parts SET quantity = ?, reorder_threshold = ? WHERE name = ? AND user_username = ?";
    private static final String SQL_SELECT_ALL = "SELECT name, quantity, reorder_threshold FROM parts WHERE user_username = ?";
    private static final String SQL_SELECT_BELOW_THRESHOLD = "SELECT name, quantity, reorder_threshold FROM parts WHERE quantity <= reorder_threshold AND user_username = ?";
    private static final String SQL_DELETE = "DELETE FROM parts WHERE name = ? AND user_username = ?";

    private String getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser().getUsername();
    }

    @Override
    public void savePart(PartEntity part) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, part.getName());
            ps.setInt(2, part.getQuantity());
            ps.setInt(3, part.getReorderThreshold());
            ps.setString(4, getCurrentUser()); // Inseriamo l'utente
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseOperationException("Errore durante il salvataggio della parte: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<PartEntity> getPartByName(String name) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_NAME)) {
            ps.setString(1, name);
            ps.setString(2, getCurrentUser()); // Filtriamo per utente
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new PartEntity(
                        rs.getString(COLUMN_NAME),
                        rs.getInt(COLUMN_QUANTITY),
                        rs.getInt(COLUMN_REORDER_THRESHOLD),
                        getCurrentUser() // Passiamo il proprietario
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Errore nel recupero della parte: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void updatePart(PartEntity part) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setInt(1, part.getQuantity());
            ps.setInt(2, part.getReorderThreshold());
            ps.setString(3, part.getName());
            ps.setString(4, getCurrentUser()); // WHERE user...
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Errore durante l'aggiornamento della parte: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PartEntity> getAllParts() {
        List<PartEntity> parts = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL)) { // Nota: ora è PreparedStatement
            ps.setString(1, getCurrentUser());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                parts.add(new PartEntity(
                        rs.getString(COLUMN_NAME),
                        rs.getInt(COLUMN_QUANTITY),
                        rs.getInt(COLUMN_REORDER_THRESHOLD),
                        getCurrentUser()
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Errore durante il recupero dei pezzi: " + e.getMessage(), e);
        }
        return parts;
    }

    @Override
    public List<PartEntity> getPartsBelowThreshold() {
        List<PartEntity> lowStock = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BELOW_THRESHOLD)) {
            ps.setString(1, getCurrentUser());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lowStock.add(new PartEntity(
                        rs.getString(COLUMN_NAME),
                        rs.getInt(COLUMN_QUANTITY),
                        rs.getInt(COLUMN_REORDER_THRESHOLD),
                        getCurrentUser()
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Errore nel recupero parti sotto soglia: " + e.getMessage(), e);
        }
        return lowStock;
    }

    @Override
    public void removePart(String name) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setString(1, name);
            ps.setString(2, getCurrentUser());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Errore durante l'eliminazione della parte: " + e.getMessage(), e);
        }
    }
}