package com.canteenbackend.helper.base.construct;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public abstract class RestfullService<T, P , S , U> {
    public abstract Page<T> getAll(P p);

    public abstract T get(UUID id);

    public abstract T store(S s);

    public abstract T update(UUID id, U  u);

    public abstract T destroy(UUID id);
}
