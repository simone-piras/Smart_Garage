package DatabaseDAO;

import DAO.InventoryDAO;
import entity.PartEntity;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseInventoryDAO implements InventoryDAO {

    @Override
    public void savePart(PartEntity part) {
        String sql = "INSERT INTO parts (name, quantity, reorder_threshold) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, part.getName());
            ps.setInt(2, part.getQuantity());
            ps.setInt(3, part.getReorderThreshold());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio della parte: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<PartEntity> getPartByName(String name) {
        String sql = "SELECT * FROM parts WHERE name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new PartEntity(
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getInt("reorder_threshold")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero della parte: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void updatePart(PartEntity part) {
        String sql = "UPDATE parts SET quantity = ?, reorder_threshold = ? WHERE name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, part.getQuantity());
            ps.setInt(2, part.getReorderThreshold());
            ps.setString(3, part.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento della parte: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PartEntity> getAllParts() {
        List<PartEntity> parts = new ArrayList<>();
        String sql = "SELECT * FROM parts";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                parts.add(new PartEntity(
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getInt("reorder_threshold")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dei pezzi: " + e.getMessage(), e);
        }
        return parts;
    }

    @Override
    public List<PartEntity> getPartsBelowThreshold() {
        List<PartEntity> lowStock = new ArrayList<>();
        String sql = "SELECT * FROM parts WHERE quantity <= reorder_threshold";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lowStock.add(new PartEntity(
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getInt("reorder_threshold")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero parti sotto soglia: " + e.getMessage(), e);
        }
        return lowStock;
    }

    @Override
    public void removePart(String name) {
        String sql = "DELETE FROM parts WHERE name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione della parte: " + e.getMessage(), e);
        }
    }
}