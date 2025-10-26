package boundary;

import bean.UserBean;
import exception.DuplicateUsernameException;
import controller.UserManager;

public class UserBoundary {
    private final UserManager userManager = new UserManager();

    public UserBean getUser(String username) {
        return userManager.getUser(username);
    }

    public boolean loginUser(String username, String password) {
        return userManager.loginUser(username, password);
    }

    public void registerUser(String username, String password, String email) throws DuplicateUsernameException {
        UserBean userBean = new UserBean();
        userBean.setUsername(username);
        userBean.setPassword(password);
        userBean.setEmail(email);
        userBean.validate();
        userManager.registerUser(userBean);
    }

    public void registerGoogleUser(String username, String email) throws DuplicateUsernameException {
        UserBean userBean = new UserBean();
        userBean.setUsername(username);
        userBean.setEmail(email);
        userBean.setPassword("google_oauth"); // Password fittizia per Google
        userBean.validate();
        userManager.registerGoogleUser(userBean);
    }

    public boolean setDefaultSupplier(String username, String supplierName) {
        UserBean user = userManager.getUser(username);
        if (user == null) return false;
        userManager.setDefaultSupplier(username, supplierName);
        user.setDefaultSupplierName(supplierName);
        return true;
    }
}
