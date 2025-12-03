package controller;

import bean.OrderBean;
import bean.OrderItemBean;
import bean.UserBean;
import enumerations.OrderStatus;
import org.junit.jupiter.api.*;
import utils.ApplicationContext;
import enumerations.PersistenceType;
import utils.SessionManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;



class OrderManagerTest {

    private OrderManager orderManager;
    private DAO.OrderDAO orderDAO;
    private final List<String> createdOrderIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        ApplicationContext.getInstance().setPersistenceType(PersistenceType.IN_MEMORY);

        // 👇 LOGIN FAKE
        UserBean testUser = new UserBean("orderUser", "pass", "ord@test.com");
        SessionManager.getInstance().login(testUser);
        orderManager = new OrderManager();
        orderDAO = ApplicationContext.getInstance().getDAOFactory().getOrderDAO();

        Field daoField = OrderManager.class.getDeclaredField("orderDAO");
        daoField.setAccessible(true);
        daoField.set(orderManager, orderDAO);
    }

    @AfterEach
    void tearDown() {
        // Pulizia ordini di test
        for (String orderId : createdOrderIds) {
            orderDAO.deleteOrder(orderId);
        }
        createdOrderIds.clear();
        SessionManager.getInstance().logout();
    }

    @Test
    void testCreaOrdine() {
        OrderBean orderBean = createTestOrder("ORD001", "Fornitore ABC", OrderStatus.CREATING);
        orderBean.validate();

        assertDoesNotThrow(() -> {
            orderManager.createOrder(orderBean);
            createdOrderIds.add("ORD001");
        });

        Optional<OrderBean> retrievedOrder = orderManager.getOrderById("ORD001");

        assertTrue(retrievedOrder.isPresent(), "L'ordine dovrebbe essere stato creato");
        OrderBean order = retrievedOrder.get();
        assertEquals("ORD001", order.getOrderID());
        assertEquals("Fornitore ABC", order.getSupplierName());
        assertEquals(OrderStatus.CREATING, order.getStatus());
        assertEquals(2, order.getItems().size());
    }

    @Test
    void testAggiornaOrdine() {
        // Crea ordine iniziale
        OrderBean initialOrder = createTestOrder("ORD002", "Fornitore XYZ", OrderStatus.CREATING);
        orderManager.createOrder(initialOrder);
        createdOrderIds.add("ORD002");

        // Modifica ordine
        OrderBean updatedOrder = createTestOrder("ORD002", "Fornitore XYZ", OrderStatus.IN_PROCESS);
        updatedOrder.validate();

        assertDoesNotThrow(() -> {
            orderManager.updateOrder(updatedOrder);
        });

        Optional<OrderBean> retrievedOrder = orderManager.getOrderById("ORD002");

        assertTrue(retrievedOrder.isPresent());
        assertEquals(OrderStatus.IN_PROCESS, retrievedOrder.get().getStatus());
    }

    @Test
    void testGetAllOrders() {
        OrderBean order1 = createTestOrder("ORD004", "Fornitore A", OrderStatus.CREATING);
        OrderBean order2 = createTestOrder("ORD005", "Fornitore B", OrderStatus.IN_PROCESS);

        orderManager.createOrder(order1);
        orderManager.createOrder(order2);
        createdOrderIds.add("ORD004");
        createdOrderIds.add("ORD005");

        List<OrderBean> allOrders = orderManager.getAllOrders();

        // Verifica che ci siano almeno i nostri ordini di test
        List<String> orderIds = allOrders.stream()
                .map(OrderBean::getOrderID)
                .filter(id -> id.equals("ORD004") || id.equals("ORD005"))
                .toList();

        assertEquals(2, orderIds.size(), "Dovrebbero esserci 2 ordini nella lista");
    }

    @Test
    void testGetOrderByIdNonEsistente() {
        Optional<OrderBean> order = orderManager.getOrderById("ORD_INESISTENTE");

        assertFalse(order.isPresent(), "Dovrebbe restituire Optional vuoto per ordine non esistente");
    }

    private OrderBean createTestOrder(String orderId, String supplierName, OrderStatus status) {
        OrderBean order = new OrderBean();
        order.setOrderID(orderId);
        order.setSupplierName(supplierName);
        order.setStatus(status);

        OrderItemBean item1 = new OrderItemBean();
        item1.setPartName("Filtro Olio");
        item1.setQuantity(5);

        OrderItemBean item2 = new OrderItemBean();
        item2.setPartName("Candela");
        item2.setQuantity(10);

        order.setItems(Arrays.asList(item1, item2));
        return order;
    }
}
