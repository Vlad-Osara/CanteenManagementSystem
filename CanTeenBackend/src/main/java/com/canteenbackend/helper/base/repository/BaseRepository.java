package com.canteenbackend.helper.base.repository;

import com.canteenbackend.utils.object.NullAwareBeanUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.function.Function;

@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    @Transactional(readOnly = true)
    default T get(ID id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Not found: " + id));
    }


    @Transactional(readOnly = true)
    default Page<T> getAll(Pageable pageable) {
        return findAll(pageable);
    }

    @Transactional
    default T update(ID id, T source) {
        T entity = findById(id).orElseThrow(() -> new EntityNotFoundException("Not found: " + id));
        NullAwareBeanUtils.copyNonNullProperties(source, entity);
        return save(entity);
    }


    @Transactional
    default T update(ID id, T source, Function<T, T> callback) {
        T entity = findById(id).orElseThrow(() -> new EntityNotFoundException("Not found: " + id));
        NullAwareBeanUtils.copyNonNullProperties(source, entity);
        T result = save(entity);
        return callback != null ? callback.apply(result) : result;
    }


    @Transactional
    default T delete(ID id) {
        T model = findById(id).orElseThrow(() -> new EntityNotFoundException("Not found: " + id));
        deleteById(id);
        return model;
    }


    @Transactional
    default T delete(ID id, Function<T, T> callback) {
        T model = findById(id).orElseThrow(() -> new EntityNotFoundException("Not found: " + id));
        deleteById(id);
        return callback != null ? callback.apply(model) : model;
    }
}