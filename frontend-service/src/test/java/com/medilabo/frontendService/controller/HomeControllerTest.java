package com.medilabo.frontendService.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HomeControllerTest {

  @Test
  void showHomeReturnsHomeViewName() {
    HomeController controller = new HomeController();
    String viewName = controller.showHome();
    assertEquals("home", viewName);
  }
}
