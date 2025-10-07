/***************************************************************************
 *
 * Pocket web backend
 * Copyright (C) 2018/2025 Antonio Salsi <passy.linux@zresa.it>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***************************************************************************/

package it.salsi.pocket.security;

import it.salsi.commons.CommonsException;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.UserRepository;
import it.salsi.pocket.services.CacheManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.regex.Pattern;

import static it.salsi.pocket.Constant.DIVISOR;
import static it.salsi.pocket.security.RSAHelper.ALGORITHM;
import static it.salsi.pocket.security.RSAHelper.KEY_SIZE;

@Log
@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final @NotNull DeviceRepository deviceRepository;
    private final @NotNull UserRepository userRepository;
    private final @NotNull EncoderHelper encoderHelper;
    private final @NotNull CacheManager cacheManager;

    // Pattern per validare UUID
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    // Pattern per validare crypt (Base64 URL safe)
    private static final Pattern CRYPT_PATTERN = Pattern.compile(
        "^[A-Za-z0-9_-]{10,2048}={0,2}$"
    );

    public AuthenticationFilter(
            @Autowired @NotNull final DeviceRepository deviceRepository,
            @Autowired @NotNull final UserRepository userRepository,
            @Autowired @NotNull final EncoderHelper encoderHelper,
            @Autowired @NotNull final CacheManager cacheManager
    ) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.encoderHelper = encoderHelper;
        this.cacheManager = cacheManager;
    }

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        final var requestURI = request.getRequestURI();
                
        // Skip authentication for non-API endpoints and for test/heartbeat endpoints (handled by controller)
        if (!requestURI.startsWith("/api/v5/") || 
            requestURI.startsWith("/api/v5/heartbeat/") || 
            requestURI.equals("/api/v5/test")) {
            
            // For API endpoints that we're skipping, set a dummy authentication context
            // so Spring Security doesn't block them
            if (requestURI.startsWith("/api/v5/")) {
                var authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
                var authToken = new UsernamePasswordAuthenticationToken("anonymous", null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
            
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract UUID and crypt from path
            final var pathParts = requestURI.split("/");
            
            // Pattern: /api/v5/{uuid}/{crypt}
            if (pathParts.length < 5) {
                sendUnauthorized(response, "Invalid API path format");
                return;
            }
            
            String uuid = pathParts[3];
            String crypt = pathParts[4];

            // Validate input format
            if (!isValidUUID(uuid)) {
                sendUnauthorized(response, "Invalid UUID format");
                return;
            }

            if (!isValidCrypt(crypt)) {
                sendUnauthorized(response, "Invalid crypt format");
                return;
            }

            // Authenticate user
            if (authenticateUser(uuid, crypt, request)) {
                // Set authentication in security context
                var authentication = new UsernamePasswordAuthenticationToken(
                        uuid, 
                        null, 
                        Collections.singletonList(new SimpleGrantedAuthority("USER"))
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                sendUnauthorized(response, "Authentication failed");
                return;
            }
        } catch (Exception e) {
            log.severe("Authentication error: " + e.getMessage());
            sendUnauthorized(response, "Internal authentication error");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidUUID(@NotNull final String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return false;
        }
        return UUID_PATTERN.matcher(uuid).matches();
    }

    private boolean isValidCrypt(String crypt) {
        if (crypt == null || crypt.length() > 2048) {
            log.warning("Crypt validation failed - null or too long: " + (crypt != null ? crypt.length() : "null"));
            return false;
        }

        return CRYPT_PATTERN.matcher(crypt).matches();
    }

    private boolean authenticateUser(@NotNull final String uuid, @NotNull final String crypt, @NotNull final HttpServletRequest request) {
        try {
            // Find device by UUID
            final var optDevice = deviceRepository.findByUuid(uuid);
            if (optDevice.isEmpty()) {
                log.warning("DEBUG: Device not found for UUID: " + uuid);
                return false;
            }

            final var device = optDevice.get();
            
            // Create RSA helper with device keys
            final var rsaHelper = new RSAHelper(ALGORITHM, KEY_SIZE);
            rsaHelper.loadPublicKey(Base64.getDecoder().decode(device.getPublicKey()));
            rsaHelper.loadPrivateKey(Base64.getDecoder().decode(device.getPrivateKey()));

            // Decrypt and validate token
            final var decrypted = rsaHelper.decryptFromURLBase64(crypt);
            final var decryptSplit = decrypted.split("[" + DIVISOR.value + "]");
            
            if (decryptSplit.length != 5) {
                log.warning("DEBUG: Invalid token parts count. Expected 5, got: " + decryptSplit.length);
                return false;
            }

            // Validate device ID
            long tokenDeviceId = Long.parseLong(decryptSplit[0]);
            if (tokenDeviceId != device.getId()) {
                log.warning("DEBUG: Device ID mismatch");
                return false;
            }

            // Validate secret is not empty
            if (decryptSplit[1].isEmpty()) {
                log.warning("DEBUG: Secret is empty");
                return false;
            }

            // Validate user credentials
            String email = decryptSplit[3];
            String hashedPassword = encoderHelper.encode(decryptSplit[4]);
            
            final var optUser = userRepository.findByEmailAndPasswd(email, hashedPassword);
            
            if (optUser.isEmpty()) {
                log.warning("DEBUG: User not found or password mismatch for email: " + email);
                return false;
            }
            
            // Update device IP and last login time
            final var remoteIP = getClientIP(request);
            device.setAddress(remoteIP);
            device.setTimestampLastLogin(java.time.Instant.now(java.time.Clock.systemUTC()).getEpochSecond());
            deviceRepository.save(device);
            
            return true;

        } catch (CommonsException | NumberFormatException e) {
            log.warning("Authentication validation failed: " + e.getMessage());
            return false;
        }
    }

    private @NotNull String getClientIP(@NotNull final HttpServletRequest request) {
        var remoteIP = request.getRemoteAddr();
        final var forwardedFor = request.getHeader("x-forwarded-for");
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            // Take the first IP in case of multiple proxies
            remoteIP = forwardedFor.split(",")[0].trim();
        }
        return remoteIP;
    }

    private void sendUnauthorized(@NotNull final HttpServletResponse response, @NotNull final String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}