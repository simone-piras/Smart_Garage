package mapper;

import bean.UserBean;
import entity.UserEntity;

public class UserMapper implements BeanEntityMapper<UserBean, UserEntity> {

    @Override
    public UserBean toBean(UserEntity entity) {
        if (entity == null) return null;

        UserBean bean = new UserBean();
        bean.setUsername(entity.getUsername());
        bean.setEmail(entity.getEmail());
        bean.setPassword(entity.getPassword());
        bean.setDefaultSupplierName(entity.getDefaultSupplierName());
        return bean;
    }

    @Override
    public UserEntity toEntity(UserBean bean) {
        if (bean == null) return null;

        return new UserEntity(
                bean.getUsername(),
                bean.getEmail(),
                bean.getPassword(),
                bean.getDefaultSupplierName()
        );
    }
}
