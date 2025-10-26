package DAO;

import entity.UserEntity;
import java.util.Optional;

public interface UserDAO {
    void saveUser(UserEntity user);
    Optional<UserEntity> getUserByUsername(String username);
    boolean userExists(String username);
    boolean validateCredentials(String username, String password);
    void updateDefaultSupplier(String username, String supplierName);
}