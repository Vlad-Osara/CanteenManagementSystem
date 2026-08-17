package com.canteenbackend.helper.paginate.dto;

import java.util.List;

public class PaginateDTO<T> {
    public int page;
    public int limit;
    public long totalItems;
    public int totalPages;
    public List<T> data;

    public PaginateDTO(int page, int limit, long totalItems, int totalPages, List<T> data) {
        this.page = page;
        this.limit = limit;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.data = data;
    }
}
