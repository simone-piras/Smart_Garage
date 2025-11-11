package controller;

import bean.NotificationBean;
import bean.PartBean;
import DAO.InventoryDAO;
import entity.PartEntity;
import mapper.BeanEntityMapperFactory;
import org.junit.jupiter.api.*;
import utils.ApplicationContext;
import utils.DAOFactory;
import enumerations.PersistenceType;
import observer.Observer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;



class InventoryManagerObserverTest {

    private InventoryManager inventoryManager;
    private InventoryDAO inventoryDAO;
    private TestObserver testObserver;
    private final List<String> createdPartNames = new ArrayList<>();  //tiene traccia delle parti create per pulizia automatica

    //creo un observer finto che implementa Observer, simula quello che fa il vero NotificationManager
    static class TestObserver implements Observer {
        private final List<NotificationBean> receivedNotifications = new ArrayList<>();

        @Override
        public void update(NotificationBean notification) {
            receivedNotifications.add(notification);
        }

        public List<NotificationBean> getNotifications() {
            return new ArrayList<>(receivedNotifications);
        }

        public void clear() {
            receivedNotifications.clear();
        }

        public int getNotificationCount() {
            return receivedNotifications.size();
        }
    }

    //servono per controllare l'Observer durante i test

    @BeforeEach
    void setUp() throws Exception {

        ApplicationContext.getInstance().setPersistenceType(PersistenceType.IN_MEMORY); //cosi che non toccano la parte file o database

        inventoryManager = new InventoryManager();
        inventoryDAO = ApplicationContext.getInstance().getDAOFactory().getInventoryDAO();
        testObserver = new TestObserver();


        Field daoField = InventoryManager.class.getDeclaredField("inventoryDAO");
        daoField.setAccessible(true);
        daoField.set(inventoryManager, inventoryDAO);


        inventoryManager.addObserver(testObserver);
    }

    @AfterEach
    void tearDown() {

        for (String partName : createdPartNames) {
            inventoryDAO.removePart(partName);
        }
        createdPartNames.clear();

        if (testObserver != null) {
            testObserver.clear();
        }
    }

    @Test
    void testObserverNotificaQuandoParteAggiuntaConScorteBasse() {

        PartBean partBean = new PartBean("Filtro Aria", 2, 5); // 2 < 5 → scorte basse
        partBean.validate();

        inventoryManager.addPart(partBean);
        createdPartNames.add("Filtro Aria");


        assertEquals(1, testObserver.getNotificationCount(),
                "L'Observer dovrebbe ricevere una notifica per scorte basse");

        NotificationBean notification = testObserver.getNotifications().get(0);
        assertTrue(notification.getMessage().contains("Scorte basse"),
                "Il messaggio deve indicare scorte basse");
        assertTrue(notification.getMessage().contains("Filtro Aria"),
                "Il messaggio deve contenere il nome della parte");
        assertEquals("Filtro Aria", notification.getPartName(),
                "La notifica deve riferirsi alla parte corretta");
        assertTrue(notification.isHasSuggestedOrder(),
                "La notifica deve avere un ordine suggerito");
    }

    @Test
    void testObserverNotificaQuandoScorteScendonoSottoSoglia() throws Exception {

        PartBean partBean = new PartBean("Candela", 10, 5); // 10 > 5 → scorte ok
        inventoryManager.addPart(partBean);
        createdPartNames.add("Candela");


        assertEquals(0, testObserver.getNotificationCount(),
                "Nessuna notifica per scorte sopra soglia");


        inventoryManager.usePart("Candela", 6); // 10 - 6 = 4 < 5 → scorte basse


        assertEquals(1, testObserver.getNotificationCount(),
                "Dovrebbe esserci notifica dopo che scorte scendono sotto soglia");

        NotificationBean notification = testObserver.getNotifications().get(0);
        assertTrue(notification.getMessage().contains("4"),
                "Il messaggio deve indicare la quantità rimanente");
    }

    @Test
    void testObserverNotificaQuandoQuantitàAggiuntaNonTriggeraNotifica() {

        PartBean partBean = new PartBean("Olio Motore", 8, 5); // 8 > 5 → scorte ok
        inventoryManager.addPart(partBean);
        createdPartNames.add("Olio Motore");

        testObserver.clear(); // Reset counter


        inventoryManager.addQuantityToPart("Olio Motore", 2); // 8 + 2 = 10 > 5 → ancora ok

        assertEquals(0, testObserver.getNotificationCount(),
                "Nessuna notifica quando scorte rimangono sopra soglia");
    }

    @Test
    void testMultipleObserversRicevonoNotifiche() {

        TestObserver secondObserver = new TestObserver();
        inventoryManager.addObserver(secondObserver);

        PartBean partBean = new PartBean("Filtro Olio", 3, 5); // 3 < 5 → scorte basse
        inventoryManager.addPart(partBean);
        createdPartNames.add("Filtro Olio");

        //Verifica che TUTTI gli observer ricevano la notifica
        assertEquals(1, testObserver.getNotificationCount(),
                "Primo observer deve ricevere notifica");
        assertEquals(1, secondObserver.getNotificationCount(),
                "Secondo observer deve ricevere notifica");

        //Verifica che le notifiche siano consistenti
        NotificationBean notif1 = testObserver.getNotifications().get(0);
        NotificationBean notif2 = secondObserver.getNotifications().get(0);

        assertEquals(notif1.getMessage(), notif2.getMessage(),
                "I messaggi delle notifiche devono essere identici");
    }

    @Test
    void testObserverNotNotificatoPerScorteNormali() {
        //TEST NEGATIVO
        PartBean partBean = new PartBean("Radiatore", 15, 5); // 15 > 5 → scorte normali
        inventoryManager.addPart(partBean);
        createdPartNames.add("Radiatore");

        //Verifica ASSENZA di notifica (come assertFalse/assertNull)
        assertEquals(0, testObserver.getNotificationCount(),
                "Nessuna notifica per parti con scorte sopra soglia");
    }

    @Test
    void testQuantitàSuggeritaCalcolataCorrettamente() {

        PartBean partBean = new PartBean("Pompa Benzina", 2, 10); // 2 < 10
        inventoryManager.addPart(partBean);
        createdPartNames.add("Pompa Benzina");

        NotificationBean notification = testObserver.getNotifications().get(0);

        //Verifica calcolo quantità suggerita: (soglia + 10) - quantità attuale
        int expectedSuggested = (10 + 10) - 2; // 18
        assertEquals(expectedSuggested, notification.getSuggestedQuantity(),
                "La quantità suggerita deve essere calcolata correttamente");
    }
}