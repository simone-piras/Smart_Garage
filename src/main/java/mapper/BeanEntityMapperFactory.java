package mapper;

import java.util.HashMap;
import java.util.Map;

public class BeanEntityMapperFactory {
    private static final BeanEntityMapperFactory instance = new BeanEntityMapperFactory();
    private final Map<Class<?>, BeanEntityMapper<?, ?>> mappers = new HashMap<>();

    private BeanEntityMapperFactory() {
        registerMapper(bean.PartBean.class, entity.PartEntity.class, new PartMapper());
        registerMapper(bean.SupplierBean.class, entity.SupplierEntity.class, new SupplierMapper());
        registerMapper(bean.OrderItemBean.class, entity.OrderItemEntity.class, new OrderItemMapper());
        registerMapper(bean.OrderBean.class, entity.OrderEntity.class, new OrderMapper());
        registerMapper(bean.NotificationBean.class, entity.NotificationEntity.class, new NotificationMapper());
        registerMapper(bean.UserBean.class, entity.UserEntity.class, new UserMapper());
    }

    public static BeanEntityMapperFactory getInstance() { return instance; }

    public <B, E> void registerMapper(Class<B> beanClass, Class<E> entityClass, BeanEntityMapper<B, E> mapper) {
        mappers.put(beanClass, mapper);
        mappers.put(entityClass, mapper);
    }

    @SuppressWarnings("unchecked")
    public <B, E> BeanEntityMapper<B, E> getMapper(Class<?> clazz) {
        return (BeanEntityMapper<B, E>) mappers.get(clazz);
    }

    public <B, E> B toBean(E entity, Class<B> beanClass) {
        BeanEntityMapper<B, E> mapper = getMapper(entity.getClass());
        return mapper != null ? mapper.toBean(entity) : null;
    }

    public <B, E> E toEntity(B bean, Class<E> entityClass) {
        BeanEntityMapper<B, E> mapper = getMapper(bean.getClass());
        return mapper != null ? mapper.toEntity(bean) : null;
    }
}
