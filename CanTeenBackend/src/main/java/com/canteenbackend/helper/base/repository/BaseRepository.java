package com.canteenbackend.helper.base.repository;

import com.canteenbackend.helper.base.construct.IBaseRepository;
import com.canteenbackend.utils.object.NullAwareBeanUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.function.Function;

@Repository
@NoArgsConstructor
@Component
public class BaseRepository<T, ID extends Serializable, R extends JpaRepository<T, ID>> implements IBaseRepository<T, ID> {

    protected R repository;
    protected Class<T> entityClass;

    public BaseRepository(R repository, Class<T> entityClass) {
        this.repository = repository;
        this.entityClass = entityClass;
    }

    @Override
    @Transactional(readOnly = true)
    public T get(ID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<T> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional
    public T save(T entity) {
        return repository.save(entity);
    }

    @Override
    @Transactional
    public T save(T entity, Function<T, T> callback) {
        T result = repository.save(entity);
        return callback != null ? callback.apply(result) : result;
    }

    @Override
    @Transactional
    public T update(ID id, T source) {
        T entity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found: " + id));
        NullAwareBeanUtils.copyNonNullProperties(source, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public T update(ID id, T source, Function<T, T> callback) {
        T entity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found: " + id));
        NullAwareBeanUtils.copyNonNullProperties(source, entity);
        T result = repository.save(entity);
        return callback != null ? callback.apply(result) : result;
    }

    @Override
    @Transactional
    public T delete(ID id) {
        T model = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found: " + id));
        repository.deleteById(id);
        return model;
    }

    @Override
    @Transactional
    public T delete(ID id, Function<T, T> callback) {
        T model = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found: " + id));
        repository.deleteById(id);
        return callback != null ? callback.apply(model) : model;
    }
}