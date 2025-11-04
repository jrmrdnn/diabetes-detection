package com.medilabo.frontendService.dto;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for paginated responses.
 */
@Getter
@Setter
public class PaginatedDto {
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}
