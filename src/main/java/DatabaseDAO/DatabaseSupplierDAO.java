package DatabaseDAO;

import DAO.SupplierDAO;
import entity.SupplierEntity;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseSupplierDAO implements SupplierDAO {

    @Override
    public void saveSupplier(SupplierEntity supplier) {
        String sql = "INSERT INTO suppliers (name, email, phone) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getEmail());
            ps.setString(3, supplier.getPhoneNumber());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel salvataggio fornitore: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<SupplierEntity> getSupplierByName(String name) {
        String sql = "SELECT * FROM suppliers WHERE name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new SupplierEntity(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        false
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero fornitore: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<SupplierEntity> getAllSuppliers() {
        List<SupplierEntity> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                suppliers.add(new SupplierEntity(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        false
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero fornitori: " + e.getMessage(), e);
        }
        return suppliers;
    }
}