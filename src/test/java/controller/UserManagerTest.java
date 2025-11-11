package controller;

import bean.UserBean;
import exception.DuplicateUsernameException;
import org.junit.jupiter.api.*;
import utils.ApplicationContext;
import enumerations.PersistenceType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class UserManagerTest {

    private UserManager userManager;
    private DAO.UserDAO userDAO;
    private final List<String> createdUsernames = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        ApplicationContext.getInstance().setPersistenceType(PersistenceType.IN_MEMORY);

        userManager = new UserManager();
        userDAO = ApplicationContext.getInstance().getDAOFactory().getUserDAO();

        Field daoField = UserManager.class.getDeclaredField("userDAO");
        daoField.setAccessible(true);
        daoField.set(userManager, userDAO);
    }

    @AfterEach
    void tearDown() {
        // Pulizia utenti di test
        for (String username : createdUsernames) {
            // Non c'è un metodo delete, quindi puliamo attraverso il DAO se necessario
        }
        createdUsernames.clear();
    }

    @Test
    void testRegistrazioneUtenteSuccesso() {
        UserBean userBean = new UserBean("mariorossi", "password123", "mario.rossi@email.com");
        userBean.validate();

        assertDoesNotThrow(() -> {
            userManager.registerUser(userBean);
            createdUsernames.add("mariorossi");
        });

        UserBean retrievedUser = userManager.getUser("mariorossi");
        assertNotNull(retrievedUser, "L'utente dovrebbe essere stato registrato");
        assertEquals("mariorossi", retrievedUser.getUsername());
        assertEquals("mario.rossi@email.com", retrievedUser.getEmail());
    }

    @Test
    void testRegistrazioneUtenteDuplicato() {
        UserBean userBean = new UserBean("userduplicato", "password123", "user@email.com");

        // Prima registrazione
        assertDoesNotThrow(() -> {
            userManager.registerUser(userBean);
            createdUsernames.add("userduplicato");
        });

        // Seconda registrazione con stesso username
        UserBean duplicateUser = new UserBean("userduplicato", "altrapassword", "altro@email.com");

        Exception exception = assertThrows(DuplicateUsernameException.class,
                () -> userManager.registerUser(duplicateUser));

        assertTrue(exception.getMessage().contains("Username già in uso"));
    }

    @Test
    void testLoginSuccesso() {
        UserBean userBean = new UserBean("userlogin", "password123", "login@email.com");

        assertDoesNotThrow(() -> {
            userManager.registerUser(userBean);
            createdUsernames.add("userlogin");
        });

        boolean loginResult = userManager.loginUser("userlogin", "password123");

        assertTrue(loginResult, "Il login dovrebbe avere successo");
    }

    @Test
    void testLoginCredenzialiErrate() {
        UserBean userBean = new UserBean("userlogin2", "password123", "login2@email.com");

        assertDoesNotThrow(() -> {
            userManager.registerUser(userBean);
            createdUsernames.add("userlogin2");
        });

        boolean loginResult = userManager.loginUser("userlogin2", "passwordSBAGLIATA");

        assertFalse(loginResult, "Il login con password errata dovrebbe fallire");
    }

    @Test
    void testLoginUtenteNonEsistente() {
        boolean loginResult = userManager.loginUser("utenenteinesistente", "password");

        assertFalse(loginResult, "Il login per utente non esistente dovrebbe fallire");
    }

    @Test
    void testGetUserNonEsistente() {
        UserBean user = userManager.getUser("utenenteinesistente");

        assertNull(user, "Dovrebbe restituire null per utente non esistente");
    }

    @Test
    void testSetDefaultSupplier() {
        UserBean userBean = new UserBean("userfornitore", "password123", "fornitore@email.com");

        assertDoesNotThrow(() -> {
            userManager.registerUser(userBean);
            createdUsernames.add("userfornitore");
        });

        // Test che il metodo non lanci eccezioni
        assertDoesNotThrow(() ->
                userManager.setDefaultSupplier("userfornitore", "Fornitore ABC")
        );
    }
}
