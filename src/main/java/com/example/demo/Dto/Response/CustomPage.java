package com.example.demo.Dto.Response;

import java.util.List;

import lombok.Data;

@Data
public class CustomPage<T> {
    private int pageNumber;
    private int size;
    private int totalElements;
    private int totalPages;
    private List<T> content;
}
