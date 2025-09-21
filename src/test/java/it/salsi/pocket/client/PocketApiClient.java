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

package it.salsi.pocket.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.salsi.pocket.models.Container;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Mock client for testing Pocket Backend API endpoints.
 * This client simulates real API calls for integration testing and development.
 */
public class PocketApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String defaultUserAgent;

    /**
     * Creates a new Pocket API client.
     *
     * @param baseUrl The base URL of the Pocket API (e.g., "http://localhost:8081")
     */
    public PocketApiClient(@NotNull String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.defaultUserAgent = "PocketApiClient/1.0";
    }

    /**
     * Gets session data from the API.
     *
     * @param uuid  The device UUID
     * @param crypt The encrypted authentication token
     * @return API response containing session data
     */
    public ApiResponse<Container> getData(@NotNull String uuid, @NotNull String crypt) {
        try {
            String url = String.format("%s/api/v5/%s/%s", baseUrl, uuid, crypt);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("User-Agent", defaultUserAgent)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return handleResponse(response, Container.class);
            
        } catch (URISyntaxException | IOException | InterruptedException e) {
            return ApiResponse.error("Request failed: " + e.getMessage());
        }
    }

    /**
     * Posts session data to the API.
     *
     * @param uuid      The device UUID
     * @param crypt     The encrypted authentication token
     * @param container The container data to persist
     * @return API response containing updated session data
     */
    public ApiResponse<Container> postData(@NotNull String uuid, @NotNull String crypt, @NotNull Container container) {
        try {
            String url = String.format("%s/api/v5/%s/%s", baseUrl, uuid, crypt);
            String jsonBody = objectMapper.writeValueAsString(container);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("User-Agent", defaultUserAgent)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return handleResponse(response, Container.class);
            
        } catch (URISyntaxException | IOException | InterruptedException e) {
            return ApiResponse.error("Request failed: " + e.getMessage());
        }
    }

    /**
     * Changes user password.
     *
     * @param uuid                    The device UUID
     * @param crypt                   The encrypted authentication token
     * @param changePasswdDataOnServer Whether to change password data on server
     * @return API response indicating success/failure
     */
    public ApiResponse<Boolean> changePassword(@NotNull String uuid, @NotNull String crypt, boolean changePasswdDataOnServer) {
        try {
            String url = String.format("%s/api/v5/%s/%s/%s", baseUrl, uuid, crypt, changePasswdDataOnServer);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("User-Agent", defaultUserAgent)
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return handleResponse(response, Boolean.class);
            
        } catch (URISyntaxException | IOException | InterruptedException e) {
            return ApiResponse.error("Request failed: " + e.getMessage());
        }
    }

    /**
     * Deletes cache record.
     *
     * @param uuid  The device UUID
     * @param crypt The encrypted authentication token
     * @return API response indicating success/failure
     */
    public ApiResponse<Void> deleteCacheRecord(@NotNull String uuid, @NotNull String crypt) {
        try {
            String url = String.format("%s/api/v5/%s/%s", baseUrl, uuid, crypt);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("User-Agent", defaultUserAgent)
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return handleVoidResponse(response);
            
        } catch (URISyntaxException | IOException | InterruptedException e) {
            return ApiResponse.error("Request failed: " + e.getMessage());
        }
    }

    /**
     * Checks cache record status.
     *
     * @param uuid  The device UUID
     * @param crypt The encrypted authentication token
     * @return API response with cache status
     */
    public ApiResponse<Void> checkCacheRecord(@NotNull String uuid, @NotNull String crypt) {
        try {
            String url = String.format("%s/api/v5/%s/%s/check", baseUrl, uuid, crypt);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("User-Agent", defaultUserAgent)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return handleVoidResponse(response);
            
        } catch (URISyntaxException | IOException | InterruptedException e) {
            return ApiResponse.error("Request failed: " + e.getMessage());
        }
    }

    /**
     * Gets application health status.
     *
     * @return API response with health information
     */
    public ApiResponse<String> getHealth() {
        try {
            String url = String.format("%s/actuator/health", baseUrl);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("User-Agent", defaultUserAgent)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return handleResponse(response, String.class);
            
        } catch (URISyntaxException | IOException | InterruptedException e) {
            return ApiResponse.error("Request failed: " + e.getMessage());
        }
    }

    /**
     * Performs async GET request to retrieve session data.
     *
     * @param uuid  The device UUID
     * @param crypt The encrypted authentication token
     * @return CompletableFuture with API response
     */
    public CompletableFuture<ApiResponse<Container>> getDataAsync(@NotNull String uuid, @NotNull String crypt) {
        try {
            String url = String.format("%s/api/v5/%s/%s", baseUrl, uuid, crypt);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("User-Agent", defaultUserAgent)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> handleResponse(response, Container.class));
            
        } catch (URISyntaxException e) {
            return CompletableFuture.completedFuture(ApiResponse.error("Invalid URL: " + e.getMessage()));
        }
    }

    /**
     * Tests API connectivity with a simple request.
     *
     * @return true if API is reachable, false otherwise
     */
    public boolean testConnection() {
        try {
            ApiResponse<String> response = getHealth();
            return response.isSuccess();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Creates a batch of test requests for load testing.
     *
     * @param uuid        The device UUID
     * @param crypt       The encrypted authentication token
     * @param requestCount Number of concurrent requests
     * @return Array of CompletableFuture responses
     */
    public CompletableFuture<ApiResponse<Container>>[] createBatchRequests(@NotNull String uuid, @NotNull String crypt, int requestCount) {
        @SuppressWarnings("unchecked")
        CompletableFuture<ApiResponse<Container>>[] futures = new CompletableFuture[requestCount];
        
        for (int i = 0; i < requestCount; i++) {
            futures[i] = getDataAsync(uuid, crypt);
        }
        
        return futures;
    }

    // Helper methods

    private <T> ApiResponse<T> handleResponse(HttpResponse<String> response, Class<T> responseType) {
        try {
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (responseType == String.class) {
                    @SuppressWarnings("unchecked")
                    T result = (T) response.body();
                    return ApiResponse.success(result, response.statusCode());
                } else {
                    T result = objectMapper.readValue(response.body(), responseType);
                    return ApiResponse.success(result, response.statusCode());
                }
            } else {
                return ApiResponse.error("HTTP " + response.statusCode() + ": " + response.body());
            }
        } catch (Exception e) {
            return ApiResponse.error("Failed to parse response: " + e.getMessage());
        }
    }

    private ApiResponse<Void> handleVoidResponse(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return ApiResponse.success(null, response.statusCode());
        } else {
            return ApiResponse.error("HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    /**
     * API Response wrapper class.
     */
    public static class ApiResponse<T> {
        private final boolean success;
        private final T data;
        private final String errorMessage;
        private final int statusCode;

        private ApiResponse(boolean success, T data, String errorMessage, int statusCode) {
            this.success = success;
            this.data = data;
            this.errorMessage = errorMessage;
            this.statusCode = statusCode;
        }

        public static <T> ApiResponse<T> success(@Nullable T data, int statusCode) {
            return new ApiResponse<>(true, data, null, statusCode);
        }

        public static <T> ApiResponse<T> error(@NotNull String errorMessage) {
            return new ApiResponse<>(false, null, errorMessage, -1);
        }

        public boolean isSuccess() {
            return success;
        }

        @Nullable
        public T getData() {
            return data;
        }

        @Nullable
        public String getErrorMessage() {
            return errorMessage;
        }

        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public String toString() {
            if (success) {
                return String.format("ApiResponse{success=true, statusCode=%d, data=%s}", statusCode, data);
            } else {
                return String.format("ApiResponse{success=false, error='%s'}", errorMessage);
            }
        }
    }
}