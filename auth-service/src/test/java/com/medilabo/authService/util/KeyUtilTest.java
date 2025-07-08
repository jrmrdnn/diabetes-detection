package com.medilabo.authService.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

class KeyUtilTest {

  @TempDir
  Path tempDir;

  private Path privateKeyFile;

  private static String convertToPem(byte[] keyBytes) {
    String base64 = Base64.getEncoder().encodeToString(keyBytes);
    StringBuilder pem = new StringBuilder();
    pem.append("-----BEGIN PRIVATE KEY-----\n");
    for (int i = 0; i < base64.length(); i += 64) {
      pem.append(base64, i, Math.min(i + 64, base64.length())).append("\n");
    }
    pem.append("-----END PRIVATE KEY-----\n");
    return pem.toString();
  }

  @BeforeEach
  void setUp() throws Exception {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    KeyPair pair = keyGen.generateKeyPair();
    byte[] pkcs8 = pair.getPrivate().getEncoded();
    String pem = convertToPem(pkcs8);

    privateKeyFile = tempDir.resolve("test_private_key.pem");
    Files.writeString(privateKeyFile, pem);
  }

  @Test
  void getPrivateKey_shouldReturnValidRSAPrivateKey() {
    KeyUtil keyUtil = new KeyUtil();
    ReflectionTestUtils.setField(
      keyUtil,
      "privateKeyPath",
      privateKeyFile.toString()
    );

    RSAPrivateKey privateKey = keyUtil.getPrivateKey();

    assertNotNull(privateKey);
    assertEquals("RSA", privateKey.getAlgorithm());
    assertTrue(privateKey.getEncoded().length > 0);
  }

  @Test
  void getPrivateKey_shouldThrowRuntimeException_whenFileDoesNotExist() {
    KeyUtil keyUtil = new KeyUtil();
    ReflectionTestUtils.setField(
      keyUtil,
      "privateKeyPath",
      tempDir.resolve("nonexistent.pem").toString()
    );

    RuntimeException ex = assertThrows(
      RuntimeException.class,
      keyUtil::getPrivateKey
    );
    assertTrue(ex.getMessage().contains("Error loading private key"));
  }

  @Test
  void getPrivateKey_shouldThrowRuntimeException_whenFileIsInvalid()
    throws IOException {
    Path invalidFile = tempDir.resolve("invalid.pem");
    Files.writeString(invalidFile, "not a valid key");

    KeyUtil keyUtil = new KeyUtil();
    ReflectionTestUtils.setField(
      keyUtil,
      "privateKeyPath",
      invalidFile.toString()
    );

    RuntimeException ex = assertThrows(
      RuntimeException.class,
      keyUtil::getPrivateKey
    );
    assertTrue(ex.getMessage().contains("Error loading private key"));
  }
}
