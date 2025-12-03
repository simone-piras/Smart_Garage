package controller;

import DAO.InventoryDAO;
import bean.NotificationBean;
import bean.UserBean;
import DAO.UserDAO;
import entity.PartEntity;
import entity.UserEntity;
import exception.DuplicateUsernameException;
import mapper.BeanEntityMapperFactory;
import utils.ApplicationContext;
import utils.SessionManager;
import utils.SharedManagers;

import java.time.LocalDate;
import java.util.Optional;

public class UserManager {
    private final UserDAO userDAO;
    private final InventoryDAO inventoryDAO;
    private final BeanEntityMapperFactory mapperFactory = BeanEntityMapperFactory.getInstance();

    public UserManager() {
        this.userDAO = ApplicationContext.getInstance().getDAOFactory().getUserDAO();
        this.inventoryDAO = ApplicationContext.getInstance().getDAOFactory().getInventoryDAO();
    }

    public void registerUser(UserBean userBean) throws DuplicateUsernameException {
        if (userDAO.userExists(userBean.getUsername())) {
            throw new DuplicateUsernameException("Username già in uso: " + userBean.getUsername());
        }
        UserEntity entity = mapperFactory.toEntity(userBean, UserEntity.class);
        userDAO.saveUser(entity);
        SessionManager.getInstance().login(userBean);
        initDefaultInventory(userBean.getUsername());
    }

    //inventario di default per i nuovi utenti
    private void initDefaultInventory(String username) {
        saveAndCheck(new PartEntity("Filtro Olio", 5, 2, username));
        saveAndCheck(new PartEntity("Filtro Aria", 4, 2, username));
        saveAndCheck(new PartEntity("Batteria Auto", 1, 1, username));
        saveAndCheck(new PartEntity("Olio Motore", 10, 5, username));
        saveAndCheck(new PartEntity("Pastiglie Freno", 4, 2, username));
        saveAndCheck(new PartEntity("Candele", 15, 7, username));
        saveAndCheck(new PartEntity("Fanale Posteriore", 15, 7, username));

        // Questo genererà sicuramente una notifica (6 <= 8)
        saveAndCheck(new PartEntity("Cinghia Distribuzione", 6, 8, username));

        // Anche questo (5 <= 6)
        saveAndCheck(new PartEntity("Liquido Freni", 5, 6, username));

        saveAndCheck(new PartEntity("Lampadina Faro", 2, 1, username));
    }


    private void saveAndCheck(PartEntity part) {
        //Salva
        inventoryDAO.savePart(part);

        //Controllo Soglia
        if (part.getQuantity() <= part.getReorderThreshold()) {
            String message = "Scorte basse per: " + part.getName() +
                    " (Quantità: " + part.getQuantity() + ", Soglia minima: " + part.getReorderThreshold() + ")";

            NotificationBean notification = new NotificationBean(
                    message,
                    null,
                    LocalDate.now().toString(),
                    part.getName()
            );

            notification.setHasSuggestedOrder(true);
            notification.setSuggestedQuantity((part.getReorderThreshold() + 10) - part.getQuantity());
            notification.validate();

            //NotificationManager condiviso per salvare la notifica
            SharedManagers.getInstance().getNotificationManager().addNotification(notification);
        }
    }

    //inizializza anche la Sessione
    public boolean loginUser(String username, String password) {
        if (userDAO.validateCredentials(username, password)) {
            // Recupero l'utente
            Optional<UserEntity> userOpt = userDAO.getUserByUsername(username);

            if (userOpt.isPresent()) {
                UserBean bean = mapperFactory.toBean(userOpt.get(), UserBean.class);

                // SALVO L'UTENTE NELLA SESSIONE
                SessionManager.getInstance().login(bean);
                return true;
            }
        }
        return false;
    }

    //
    public boolean loginWithGoogle(String email) {

        String username = email.split("@")[0];

        //Cerchiamo l'utente
        Optional<UserEntity> userOpt = userDAO.getUserByUsername(username);

        if (userOpt.isPresent()) {
            UserBean bean = mapperFactory.toBean(userOpt.get(), UserBean.class);


            //SALVO LA SESSIONE
            SessionManager.getInstance().login(bean);
            return true;
        }
        return false; // Utente non trovato
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

        // Se si registra, lo logghiamo anche direttamente
        SessionManager.getInstance().login(userBean);

        initDefaultInventory(userBean.getUsername());
    }

    public void setDefaultSupplier(String username, String supplierName) {
        userDAO.updateDefaultSupplier(username, supplierName);

        // Aggiorniamo anche l'utente in sessione se è lui
        UserBean currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUsername().equals(username)) {
            currentUser.setDefaultSupplierName(supplierName);
        }
    }

    // Metodo di utilità per il logout
    public void logout() {
        SessionManager.getInstance().logout();

    }
}
