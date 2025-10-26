package controller;

import bean.UserBean;
import DAO.UserDAO;
import entity.UserEntity;
import exception.DuplicateUsernameException;
import mapper.BeanEntityMapperFactory;
import utils.ApplicationContext;

public class UserManager {
    private final UserDAO userDAO;
    private final BeanEntityMapperFactory mapperFactory = BeanEntityMapperFactory.getInstance();

    public UserManager() {
        this.userDAO = ApplicationContext.getInstance().getDAOFactory().getUserDAO();
    }

    public void registerUser(UserBean userBean) throws DuplicateUsernameException {
        if (userDAO.userExists(userBean.getUsername())) {
            throw new DuplicateUsernameException("Username già in uso: " + userBean.getUsername());
        }
        UserEntity entity = mapperFactory.toEntity(userBean, UserEntity.class);
        userDAO.saveUser(entity);
    }

    public boolean loginUser(String username, String password) {
        return userDAO.validateCredentials(username, password);
    }

    public UserBean getUser(String username) {
        return userDAO.getUserByUsername(username)
                .map(entity -> mapperFactory.toBean(entity, UserBean.class))
                .orElse(null);
    }

    public void registerGoogleUser(UserBean userBean) throws DuplicateUsernameException {
        if (userDAO.userExists(userBean.getUsername())) {
            throw new DuplicateUsernameException("Username già in uso con Google: " + userBean.getUsername());
        }
        UserEntity entity = mapperFactory.toEntity(userBean, UserEntity.class);
        userDAO.saveUser(entity);
    }

    public void setDefaultSupplier(String username, String supplierName) {
        userDAO.updateDefaultSupplier(username, supplierName);
    }
}
