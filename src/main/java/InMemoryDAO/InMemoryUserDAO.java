package InMemoryDAO;

import DAO.UserDAO;
import entity.UserEntity;
import java.util.*;

public class InMemoryUserDAO implements UserDAO {
    private final Map<String, UserEntity> userStorage = new HashMap<>();
    private int nextId = 1;

    @Override
    public void saveUser(UserEntity user) {
        if (user.getId() == 0) {
            UserEntity newUser = new UserEntity(nextId++, user.getUsername(), user.getEmail(),
                    user.getPassword(), user.getDefaultSupplierName());
            userStorage.put(newUser.getUsername(), newUser);
        } else {
            userStorage.put(user.getUsername(), user);
        }
    }

    @Override
    public Optional<UserEntity> getUserByUsername(String username) {
        return Optional.ofNullable(userStorage.get(username));
    }

    @Override
    public boolean userExists(String username) {
        return userStorage.containsKey(username);
    }

    @Override
    public boolean validateCredentials(String username, String password) {
        UserEntity user = userStorage.get(username);
        return user != null && user.getPassword().equals(password);
    }

    @Override
    public void updateDefaultSupplier(String username, String supplierName) {
        userStorage.computeIfPresent(username, (key, user) ->
                new UserEntity(user.getId(), user.getUsername(), user.getEmail(),
                        user.getPassword(), supplierName)
        );
    }
}
