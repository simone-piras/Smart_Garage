package mapper;

import bean.PartBean;
import entity.PartEntity;
import utils.SessionManager;

public class PartMapper implements BeanEntityMapper<PartBean, PartEntity> {

    @Override
    public PartBean toBean(PartEntity entity) {
        if (entity == null) return null;

        PartBean bean = new PartBean();
        bean.setName(entity.getName());
        bean.setQuantity(entity.getQuantity());
        bean.setReorderThreshold(entity.getReorderThreshold());
        return bean;
    }

    @Override
    public PartEntity toEntity(PartBean bean) {
        if (bean == null) return null;

        // RECUPERO L'UTENTE LOGGATO
        String currentUser = SessionManager.getInstance().getCurrentUser().getUsername();

        // Passo l'utente al costruttore della Entity
        return new PartEntity(bean.getName(), bean.getQuantity(), bean.getReorderThreshold(), currentUser);
    }
}