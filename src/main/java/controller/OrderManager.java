package controller;

import bean.OrderBean;
import DAO.OrderDAO;
import entity.OrderEntity;
import mapper.BeanEntityMapperFactory;
import utils.ApplicationContext;

import java.util.List;
import java.util.Optional;

public class OrderManager {
    private final OrderDAO orderDAO;
    private final BeanEntityMapperFactory mapperFactory = BeanEntityMapperFactory.getInstance();

    public OrderManager() {
        this.orderDAO = ApplicationContext.getInstance().getDAOFactory().getOrderDAO();
    }

    public void createOrder(OrderBean orderBean) {
        OrderEntity entity = mapperFactory.toEntity(orderBean, OrderEntity.class);
        orderDAO.saveOrder(entity);
    }

    public void updateOrder(OrderBean orderBean) {
        OrderEntity entity = mapperFactory.toEntity(orderBean, OrderEntity.class);
        orderDAO.updateOrder(entity);
    }

    public boolean cancelOrder(String orderId) {
        return orderDAO.deleteOrder(orderId);
    }

    public Optional<OrderBean> getOrderById(String orderId) {
        return orderDAO.getOrderByID(orderId)
                .map(entity -> mapperFactory.toBean(entity, OrderBean.class));
    }

    public List<OrderBean> getAllOrders() {
        return orderDAO.getAllOrders().stream()
                .map(entity -> mapperFactory.toBean(entity, OrderBean.class))
                .toList();
    }
}
