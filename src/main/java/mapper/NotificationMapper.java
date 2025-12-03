package mapper;

import bean.NotificationBean;
import entity.NotificationEntity;
import utils.SessionManager;

public class NotificationMapper implements BeanEntityMapper<NotificationBean, NotificationEntity> {

    @Override
    public NotificationBean toBean(NotificationEntity entity) {
        if (entity == null) return null;

        NotificationBean bean = new NotificationBean();
        bean.setMessage(entity.getMessage());
        bean.setDate(entity.getDate());
        bean.setPartName(entity.getPartName());
        bean.setHasSuggestedOrder(entity.isHasSuggestedOrder());
        bean.setSuggestedQuantity(entity.getSuggestedQuantity());

        if (entity.getRelatedOrder() != null) {
            bean.setRelatedOrder(new OrderMapper().toBean(entity.getRelatedOrder()));
        }

        return bean;
    }

    @Override
    public NotificationEntity toEntity(NotificationBean bean) {
        if (bean == null) return null;

        // RECUPERO L'UTENTE LOGGATO
        String currentUser = SessionManager.getInstance().getCurrentUser().getUsername();

        return new NotificationEntity(
                bean.getMessage(),
                bean.getDate(),
                bean.getPartName(),
                bean.isHasSuggestedOrder(),
                bean.getSuggestedQuantity(),
                bean.getRelatedOrder() != null ? new OrderMapper().toEntity(bean.getRelatedOrder()) : null,
                currentUser
        );
    }
}