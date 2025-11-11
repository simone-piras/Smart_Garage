package controller;

import bean.NotificationBean;
import bean.PartBean;
import DAO.InventoryDAO;
import entity.PartEntity;
import exception.InsufficientStockException;
import exception.PartNotFoundException;
import mapper.BeanEntityMapperFactory;
import org.junit.jupiter.api.*;
import utils.ApplicationContext;
import utils.DAOFactory;
import enumerations.PersistenceType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;



class InventoryManagerTest {

    private InventoryManager inventoryManager;
    private InventoryDAO inventoryDAO;
    private final List<String> createdPartNames = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        // Configura persistenza in memoria per i test
        ApplicationContext.getInstance().setPersistenceType(PersistenceType.IN_MEMORY);

        inventoryManager = new InventoryManager();
        inventoryDAO = ApplicationContext.getInstance().getDAOFactory().getInventoryDAO();

        // Injection del DAO tramite reflection
        Field daoField = InventoryManager.class.getDeclaredField("inventoryDAO");
        daoField.setAccessible(true);
        daoField.set(inventoryManager, inventoryDAO);
    }

    @AfterEach
    void tearDown() {
        // Pulizia dopo ogni test
        for (String partName : createdPartNames) {
            inventoryDAO.removePart(partName);
        }
        createdPartNames.clear();
    }

    @Test
    void testAggiungiParte() {
        PartBean partBean = new PartBean("Filtro Olio", 50, 10);
        partBean.validate();

        inventoryManager.addPart(partBean);
        createdPartNames.add("Filtro Olio");

        List<PartBean> allParts = inventoryManager.getAllParts();
        Optional<PartBean> foundPart = allParts.stream()
                .filter(p -> "Filtro Olio".equals(p.getName()))
                .findFirst();

        assertTrue(foundPart.isPresent(), "La parte dovrebbe essere stata aggiunta");
        PartBean part = foundPart.get();
        assertEquals("Filtro Olio", part.getName());
        assertEquals(50, part.getQuantity());
        assertEquals(10, part.getReorderThreshold());
    }

    @Test
    void testUsePartSuccesso() throws Exception {
        // Setup - aggiungi una parte
        PartBean partBean = new PartBean("Candela", 20, 5);
        inventoryManager.addPart(partBean);
        createdPartNames.add("Candela");

        // Test - usa 5 unità
        boolean result = inventoryManager.usePart("Candela", 5);

        assertTrue(result, "L'uso della parte dovrebbe avere successo");

        PartBean updatedPart = inventoryManager.getAllParts().stream()
                .filter(p -> "Candela".equals(p.getName()))
                .findFirst()
                .orElseThrow();

        assertEquals(15, updatedPart.getQuantity(), "La quantità dovrebbe essere diminuita di 5");
    }

    @Test
    void testUsePartScorteInsufficienti() {
        PartBean partBean = new PartBean("Pompa Benzina", 3, 5);
        inventoryManager.addPart(partBean);
        createdPartNames.add("Pompa Benzina");

        Exception exception = assertThrows(InsufficientStockException.class,
                () -> inventoryManager.usePart("Pompa Benzina", 5));

        assertTrue(exception.getMessage().contains("Scorte insufficienti"));
    }

    @Test
    void testUsePartNonTrovata() {
        Exception exception = assertThrows(PartNotFoundException.class,
                () -> inventoryManager.usePart("ParteInesistente", 1));

        assertTrue(exception.getMessage().contains("Parte non trovata"));
    }

    @Test
    void testGetPartsBelowThreshold() {
        // Parte sotto soglia
        PartBean lowStockPart = new PartBean("Filtro Aria", 3, 5);
        inventoryManager.addPart(lowStockPart);
        createdPartNames.add("Filtro Aria");

        // Parte sopra soglia
        PartBean normalStockPart = new PartBean("Olio Motore", 15, 5);
        inventoryManager.addPart(normalStockPart);
        createdPartNames.add("Olio Motore");

        List<PartBean> lowStockParts = inventoryManager.getPartsBelowThreshold();

        assertEquals(1, lowStockParts.size(), "Dovrebbe esserci solo una parte sotto soglia");
        assertEquals("Filtro Aria", lowStockParts.get(0).getName());
        assertEquals(3, lowStockParts.get(0).getQuantity());
    }

    @Test
    void testAggiungiQuantitàParte() {
        PartBean partBean = new PartBean("Radiatore", 10, 5);
        inventoryManager.addPart(partBean);
        createdPartNames.add("Radiatore");

        boolean result = inventoryManager.addQuantityToPart("Radiatore", 5);

        assertTrue(result, "L'aggiunta di quantità dovrebbe avere successo");

        PartBean updatedPart = inventoryManager.getAllParts().stream()
                .filter(p -> "Radiatore".equals(p.getName()))
                .findFirst()
                .orElseThrow();

        assertEquals(15, updatedPart.getQuantity(), "La quantità dovrebbe essere aumentata di 5");
    }

    @Test
    void testAggiungiQuantitàParteNonEsistente() {
        boolean result = inventoryManager.addQuantityToPart("ParteInesistente", 5);

        assertFalse(result, "L'aggiunta a parte inesistente dovrebbe fallire");
    }
}
