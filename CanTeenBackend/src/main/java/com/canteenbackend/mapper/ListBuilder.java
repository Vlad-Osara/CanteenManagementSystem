package com.canteenbackend.mapper;

import java.util.ArrayList;
import java.util.List;

public class ListBuilder<T> {
    private final List<T> list = new ArrayList<>();

    public ListBuilder<T> add(T item) {
        list.add(item);
        return this;
    }

    public List<T> build() {
        return list;
    }
}
