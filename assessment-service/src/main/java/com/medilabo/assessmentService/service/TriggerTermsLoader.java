package com.medilabo.assessmentService.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Service to load and provide categorized trigger terms from a JSON file.
 * The terms are categorized by their type (e.g., "symptoms", "lifestyle", etc.).
 */
@Slf4j
@Getter
@Component
public class TriggerTermsLoader {

  private Map<String, List<String>> categorizedTerms;

  /**
   * Retrieves the resource as an InputStream.
   * This method is used to load the JSON file containing trigger terms.
   * @param path the path to the resource file
   * @return InputStream of the resource
   */
  InputStream getResourceAsStream(String path) {
    return getClass().getResourceAsStream(path);
  }

  /**
   * Loads the trigger terms from a JSON file into a categorized map.
   * The JSON file should be located in the resources directory.
   * If an error occurs during loading, it logs the error and throws a RuntimeException.
   * This method is called automatically after the component is constructed.
   * @throws RuntimeException if there is an error reading the JSON file
   * @see ObjectMapper
   * @see TypeReference
   * @see Map
   * @see List
   * @see InputStream
   * @see TriggerTermsLoader
   **/
  @PostConstruct
  public void loadTerms() {
    ObjectMapper mapper = new ObjectMapper();
    try (InputStream is = getResourceAsStream("/trigger-terms.json")) {
      TypeReference<Map<String, List<String>>> typeRef =
        new TypeReference<>() {};
      categorizedTerms = mapper.readValue(is, typeRef);
    } catch (Exception e) {
      log.error("Error loading trigger terms from JSON file", e);
      throw new RuntimeException("Error loading trigger terms");
    }
  }
}
