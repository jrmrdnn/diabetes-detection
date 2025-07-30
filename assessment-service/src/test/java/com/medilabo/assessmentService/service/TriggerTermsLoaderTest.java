package com.medilabo.assessmentService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TriggerTermsLoaderTest {

  @Test
  void loadTerms_success() throws Exception {
    TriggerTermsLoader loader = spy(new TriggerTermsLoader());

    String jsonContent =
      "{\"symptoms\":[\"toux\",\"fièvre\"],\"lifestyle\":[\"tabac\"]}";
    InputStream mockStream = new ByteArrayInputStream(jsonContent.getBytes());

    doReturn(mockStream)
      .when(loader)
      .getResourceAsStream("/trigger-terms.json");

    loader.loadTerms();

    Map<String, List<String>> result = loader.getCategorizedTerms();
    assertNotNull(result);
    assertEquals(2, result.size());
    assertIterableEquals(List.of("toux", "fièvre"), result.get("symptoms"));
    assertIterableEquals(List.of("tabac"), result.get("lifestyle"));
  }

  @Test
  void loadTerms_fileNotFound() {
    TriggerTermsLoader loader = spy(new TriggerTermsLoader());

    doReturn(null).when(loader).getResourceAsStream("/trigger-terms.json");

    RuntimeException exception = assertThrows(
      RuntimeException.class,
      loader::loadTerms
    );

    assertNotNull(exception);
  }

  @Test
  void loadTerms_Exception() throws Exception {
    TriggerTermsLoader loader = spy(new TriggerTermsLoader());

    InputStream brokenStream = mock(InputStream.class);
    when(brokenStream.read(any())).thenThrow(new IOException("Test error"));

    doReturn(brokenStream)
      .when(loader)
      .getResourceAsStream("/trigger-terms.json");

    RuntimeException exception = assertThrows(
      RuntimeException.class,
      loader::loadTerms
    );

    assertNotNull(exception);
    assertEquals("Error loading trigger terms", exception.getMessage());
    verify(loader).getResourceAsStream("/trigger-terms.json");
  }
}
