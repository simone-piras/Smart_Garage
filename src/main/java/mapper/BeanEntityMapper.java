package mapper;

public interface BeanEntityMapper<B, E> {
    B toBean(E entity);
    E toEntity(B bean);
}

