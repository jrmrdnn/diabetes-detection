package com.medilabo.authService.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.medilabo.authService.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for generating JWT tokens.
 * It uses RSA512 algorithm and includes user information in the token.
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class JwtUtil {

  private final KeyUtil keyUtil;

  @Value("${jwt.expiration}")
  private long expiration;

  /**
   * Generates a JWT token for the given user.
   *
   * <p>
   *  This method creates a JWT token using the RSA512 algorithm,
   *  including the user's username, ID, role, and IP address.
   *  The token is signed with the private key loaded from the KeyUtil.
   *  The token is set to expire after a specified duration.
   * </p>
   * @param userId the ID of the user
   * @param username the username of the user
   * @param httpRequest the HTTP request to extract the user's IP address
   * @return a signed JWT token as a string
   * @throws RuntimeException if there is an error during token generation
   * @see Algorithm
   * @see JWT
   * @see KeyUtil
   */
  public String generateToken(
    String userId,
    String username,
    HttpServletRequest httpRequest
  ) {
    try {
      Algorithm algorithm = Algorithm.RSA512(null, keyUtil.getPrivateKey());
      return JWT.create()
        .withSubject(username)
        .withClaim("id", userId)
        .withClaim("role", "USER")
        .withClaim("ip", httpRequest.getRemoteAddr())
        .withIssuer("auth-service")
        .withExpiresAt(Instant.now().plus(expiration, ChronoUnit.HOURS))
        .sign(algorithm);
    } catch (Exception e) {
      log.error("Error generating JWT token", e);
      throw new RuntimeException("Error generating JWT token");
    }
  }
}
