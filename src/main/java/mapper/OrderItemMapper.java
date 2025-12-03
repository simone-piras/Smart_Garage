package mapper;

import bean.OrderItemBean;
import entity.OrderItemEntity;
import entity.PartEntity;
import utils.SessionManager;

public class OrderItemMapper implements BeanEntityMapper<OrderItemBean, OrderItemEntity> {

    @Override
    public OrderItemBean toBean(OrderItemEntity entity) {
        if (entity == null) return null;

        OrderItemBean bean = new OrderItemBean();
        bean.setPartName(entity.getPartName());
        bean.setQuantity(entity.getQuantity());
        return bean;
    }

    @Override
    public OrderItemEntity toEntity(OrderItemBean bean) {
        if (bean == null) return null;

        // Recuperiamo l'utente corrente
        String currentUser = SessionManager.getInstance().getCurrentUser().getUsername();


        PartEntity partEntity = new PartEntity(bean.getPartName(), 0, 0, currentUser);

        return new OrderItemEntity(
                partEntity,
                bean.getQuantity(),
                null
        );
    }
}