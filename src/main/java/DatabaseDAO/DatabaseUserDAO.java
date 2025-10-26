package DatabaseDAO;

import DAO.UserDAO;
import entity.UserEntity;
import utils.DBConnection;

import java.sql.*;
import java.util.Optional;

public class DatabaseUserDAO implements UserDAO {

    @Override
    public void saveUser(UserEntity user) {
        String sql = "INSERT INTO users (username, password, email, default_supplier) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getDefaultSupplierName());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel salvataggio utente: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<UserEntity> getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new UserEntity(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("default_supplier")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero utente: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel controllo utente: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateCredentials(String username, String password) {
        String sql = "SELECT 1 FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nella validazione credenziali: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateDefaultSupplier(String username, String supplierName) {
        String sql = "UPDATE users SET default_supplier = ? WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplierName);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'aggiornamento fornitore predefinito: " + e.getMessage(), e);
        }
    }
}