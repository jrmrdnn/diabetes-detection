package com.medilabo.frontendService.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginatedDto {
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}
