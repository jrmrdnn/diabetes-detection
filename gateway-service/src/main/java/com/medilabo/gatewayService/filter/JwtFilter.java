package com.medilabo.gatewayService.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.medilabo.gatewayService.constant.SecurityConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

/**
 * JWT Filter for the gateway service.
 * This filter checks for JWT tokens in requests and validates them.
 * If the token is valid, it sets the authentication context; otherwise, it redirects to the login page.
 */
@Slf4j
@Component
public class JwtFilter implements GlobalFilter, Ordered {

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    @Value("${cookie.auth-name}")
    private String authCookieName;

    /**
     * Filters the incoming request to check for JWT token validity.
     * If the token is valid, it sets the authentication context; otherwise, it redirects to the login page.
     *
     * @param exchange the server web exchange
     * @param chain    the gateway filter chain
     * @return a Mono that completes when the filter processing is done
     */
    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String requestPath = request.getURI().getPath();

        for (String publicEndpoint : SecurityConstants.PUBLIC_ENDPOINTS) {
            if (
                    requestPath.equals(publicEndpoint) ||
                            (publicEndpoint.contains("*") &&
                                    requestPath.matches(publicEndpoint.replace("*", ".*")))
            ) {
                return chain.filter(exchange);
            }
        }

        String token = extractToken(request);
        if (token == null) return clearAuthTokenAndRedirect(response, "/login#error=missing_token");


        try {
            Algorithm algorithm = Algorithm.RSA512(parsePemPublicKey(), null);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth-service").build();

            DecodedJWT jwt = verifier.verify(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            jwt.getSubject(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + jwt.getClaim("role").asString())
                            )
                    );

            return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        } catch (JWTVerificationException e) {
            return clearAuthTokenAndRedirect(response, "/login#error=invalid_token");
        }
    }

    /**
     * Parses the public key from the specified PEM file path.
     *
     * @return the parsed RSAPublicKey
     */
    private RSAPublicKey parsePemPublicKey() {
        try {
            String keyContent = Files.readString(Path.of(publicKeyPath));
            String publicKeyPEM = keyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (
                IOException | NoSuchAlgorithmException | InvalidKeySpecException e
        ) {
            log.error("Error loading public key from path: {}", publicKeyPath, e);
            throw new RuntimeException("Error loading public key");
        }
    }

    /**
     * Clears the authentication token cookie and redirects to the specified URL.
     *
     * @param response    the server HTTP response
     * @param redirectUrl the URL to redirect to
     * @return a Mono that completes when the response is set
     */
    private Mono<Void> clearAuthTokenAndRedirect(ServerHttpResponse response, String redirectUrl) {
        ResponseCookie cookie = ResponseCookie.from(authCookieName, "")
                .maxAge(0)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .build();

        response.addCookie(cookie);
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().add(HttpHeaders.LOCATION, redirectUrl);

        return response.setComplete();
    }

    /**
     * Extracts the JWT token from the request headers or cookies.
     *
     * @param request the server HTTP request
     * @return the extracted token, or null if not found
     */
    private String extractToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst("Authorization");
        if (header != null && header.startsWith("Bearer ")) return header.substring(7);

        HttpCookie cookie = request.getCookies().getFirst(authCookieName);
        if (cookie != null) return cookie.getValue();

        return null;
    }

    /**
     * Returns the order of this filter in the filter chain.
     * This filter should run before other filters to ensure JWT validation.
     *
     * @return the order value
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
