package controller;

import bean.PartBean;
import bean.UserBean;
import org.junit.jupiter.api.*;
import utils.ApplicationContext;
import utils.SessionManager;
import enumerations.PersistenceType;

import static org.junit.jupiter.api.Assertions.*;

class MultiUserTest {

    private InventoryManager inventoryManager;

    @BeforeEach
    void setUp() {
        ApplicationContext.getInstance().setPersistenceType(PersistenceType.IN_MEMORY);
        inventoryManager = new InventoryManager();
    }

    @AfterEach
    void tearDown() {
        SessionManager.getInstance().logout();
    }

    @Test
    void testIsolamentoDatiTraUtenti() {
        // --- UTENTE A ---
        UserBean userA = new UserBean("Alice", "pass", "alice@test.com");
        SessionManager.getInstance().login(userA);

        // Alice aggiunge "Olio" (qta: 10)
        PartBean olioAlice = new PartBean("Olio", 10, 2);
        inventoryManager.addPart(olioAlice);

        // Verifica che Alice lo veda
        assertEquals(1, inventoryManager.getAllParts().size());
        assertEquals(10, inventoryManager.getPartByName("Olio").get().getQuantity());

        // --- CAMBIO UTENTE -> BOB ---
        SessionManager.getInstance().logout();
        UserBean userB = new UserBean("Bob", "pass", "bob@test.com");
        SessionManager.getInstance().login(userB);

        // Bob NON deve vedere l'olio di Alice
        assertEquals(0, inventoryManager.getAllParts().size(), "Bob non dovrebbe vedere l'inventario di Alice");

        // Bob aggiunge il SUO "Olio" (qta: 50)
        PartBean olioBob = new PartBean("Olio", 50, 5);
        inventoryManager.addPart(olioBob);

        // Verifica che Bob veda il suo
        assertEquals(1, inventoryManager.getAllParts().size());
        assertEquals(50, inventoryManager.getPartByName("Olio").get().getQuantity());

        // --- TORNA ALICE ---
        SessionManager.getInstance().logout();
        SessionManager.getInstance().login(userA);

        // Alice deve ritrovare i suoi 10, non i 50 di Bob
        assertEquals(10, inventoryManager.getPartByName("Olio").get().getQuantity(),
                "Alice deve vedere i suoi dati originali, non quelli di Bob");
    }
}
