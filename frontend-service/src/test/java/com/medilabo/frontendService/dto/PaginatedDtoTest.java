package com.medilabo.frontendService.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PaginatedDtoTest {

  @Test
  void defaultValuesShouldBeZero() {
    PaginatedDto dto = new PaginatedDto();
    assertEquals(
      0L,
      dto.getTotalElements(),
      "totalElements should default to 0"
    );
    assertEquals(0, dto.getTotalPages(), "totalPages should default to 0");
    assertEquals(0, dto.getCurrentPage(), "currentPage should default to 0");
    assertEquals(0, dto.getPageSize(), "pageSize should default to 0");
  }

  @Test
  void gettersAndSettersShouldWorkCorrectly() {
    PaginatedDto dto = new PaginatedDto();
    dto.setTotalElements(12345L);
    dto.setTotalPages(5);
    dto.setCurrentPage(2);
    dto.setPageSize(50);

    assertEquals(
      12345L,
      dto.getTotalElements(),
      "getTotalElements should return the value set"
    );
    assertEquals(
      5,
      dto.getTotalPages(),
      "getTotalPages should return the value set"
    );
    assertEquals(
      2,
      dto.getCurrentPage(),
      "getCurrentPage should return the value set"
    );
    assertEquals(
      50,
      dto.getPageSize(),
      "getPageSize should return the value set"
    );
  }
}
