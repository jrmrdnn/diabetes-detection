package com.medilabo.authService.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for generating RSA keys.
 * It initializes a public and private key pair on application startup.
 */
@Log4j2
@Component
public class KeyUtil {

  @Value("${jwt.private-key-path}")
  private String privateKeyPath;

  /**
   * Loads and returns the RSA private key from the configured file path.
   *
   * <p>
   *  This method reads the private key file in PEM format, removes the PEM headers
   *  and footers, decodes the Base64 content, and constructs an RSAPrivateKey object
   *  using the PKCS#8 encoding specification.
   * </p>
   *
   * @return the RSA private key loaded from the file
   * @throws RuntimeException if the private key cannot be loaded due to:
   * <ul>
   *  <li>IOException - if the key file cannot be read</li>
   *  <li>NoSuchAlgorithmException - if RSA algorithm is not available</li>
   *  <li>InvalidKeySpecException - if the key format is invalid</li>
   * </ul>
   * @see RSAPrivateKey
   * @see PKCS8EncodedKeySpec
   */
  public RSAPrivateKey getPrivateKey() {
    try {
      String keyContent = Files.readString(Path.of(privateKeyPath));
      String privateKeyPEM = keyContent
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replaceAll("\\s", "");

      byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
      PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      return (RSAPrivateKey) keyFactory.generatePrivate(spec);
    } catch (Exception e) {
      log.error("Error loading private key: {}", e.getMessage());
      throw new RuntimeException("Error loading private key");
    }
  }
}
