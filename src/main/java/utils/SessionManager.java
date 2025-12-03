package utils;

import bean.UserBean;

public class SessionManager {

    private static SessionManager instance = null;
    private UserBean currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(UserBean user) {
        this.currentUser = user;
    }

    public UserBean getCurrentUser() {
        return this.currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }
}