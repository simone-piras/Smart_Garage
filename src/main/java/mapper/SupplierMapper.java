package mapper;

import bean.SupplierBean;
import entity.SupplierEntity;

public class SupplierMapper implements BeanEntityMapper<SupplierBean, SupplierEntity> {

    @Override
    public SupplierBean toBean(SupplierEntity entity) {
        if (entity == null) return null;

        SupplierBean bean = new SupplierBean();
        bean.setName(entity.getName());
        bean.setEmail(entity.getEmail());
        bean.setPhone(entity.getPhone());
        return bean;
    }

    @Override
    public SupplierEntity toEntity(SupplierBean bean) {
        if (bean == null) return null;


        return new SupplierEntity(bean.getName(), bean.getEmail(), bean.getPhone(), false);
    }
}