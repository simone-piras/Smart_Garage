package mapper;

import bean.PartBean;
import entity.PartEntity;

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
        return new PartEntity(bean.getName(), bean.getQuantity(), bean.getReorderThreshold());
    }
}
