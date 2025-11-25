package mapper;

import bean.OrderBean;
import entity.OrderEntity;
import entity.SupplierEntity;
import enumerations.OrderStatus;


public class OrderMapper implements BeanEntityMapper<OrderBean, OrderEntity> {

    @Override
    public OrderBean toBean(OrderEntity entity) {
        if (entity == null) return null;

        OrderBean bean = new OrderBean();
        bean.setOrderID(entity.getId());

        if (entity.getStatus() != null) {
            try {
                OrderStatus status = OrderStatus.valueOf(entity.getStatus().toUpperCase().replace(" ", "_"));
                bean.setStatus(status);
            } catch (IllegalArgumentException _) {
                // Se non trova corrispondenza, usa stato di default
                bean.setStatus(OrderStatus.CREATING);
            }
        }

        bean.setSupplierName(entity.getSupplierName());

        if (entity.getItems() != null) {
            bean.setItems(entity.getItems().stream()
                    .map(item -> new OrderItemMapper().toBean(item))
                    .toList());
        }

        return bean;
    }

    @Override
    public OrderEntity toEntity(OrderBean bean) {
        if (bean == null) return null;

        SupplierEntity supplierEntity = null;
        if (bean.getSupplierName() != null) {
            supplierEntity = new SupplierEntity(bean.getSupplierName(), null, null, false);
        }

        String statusString = bean.getStatus() != null ? bean.getStatus().toString() : "CREATING";

        OrderEntity orderEntity = new OrderEntity(
                supplierEntity,
                bean.getItems() != null ?
                        bean.getItems().stream()
                                .map(item -> new OrderItemMapper().toEntity(item))
                                .toList() : null,
                statusString,
                null
        );

        if (bean.getOrderID() != null && !bean.getOrderID().isEmpty()) {
            orderEntity.setId(bean.getOrderID());
        }

        return orderEntity;
    }
}