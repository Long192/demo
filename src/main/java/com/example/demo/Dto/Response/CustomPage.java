package com.example.demo.Dto.Response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomPage<T> {
    private int pageNumber;
    private int size;
    private int totalElements;
    private int totalPages;
    private List<T> content;
}
