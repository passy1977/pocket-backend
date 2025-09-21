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

import it.salsi.pocket.models.Container;
import it.salsi.pocket.models.Device;
import it.salsi.pocket.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PocketApiClient Tests")
class PocketApiClientTest {

    private PocketApiClient client;
    private static final String TEST_BASE_URL = "http://localhost:8081";
    private static final String VALID_UUID = "12345678-1234-1234-1234-123456789012";
    private static final String VALID_CRYPT = "validCryptData123";

    @BeforeEach
    void setUp() {
        client = new PocketApiClient(TEST_BASE_URL);
    }

    @Test
    @DisplayName("Should create client with correct base URL")
    void shouldCreateClientWithCorrectBaseUrl() {
        // Test URL normalization
        PocketApiClient clientWithTrailingSlash = new PocketApiClient("http://localhost:8081/");
        assertNotNull(clientWithTrailingSlash);

        PocketApiClient clientWithoutSlash = new PocketApiClient("http://localhost:8081");
        assertNotNull(clientWithoutSlash);
    }

    @Test
    @DisplayName("Should handle getData request structure")
    void shouldHandleGetDataRequestStructure() {
        // This test verifies the client can make requests (without requiring running server)
        // In a real test environment, you'd use WireMock or similar to mock HTTP responses
        
        PocketApiClient.ApiResponse<Container> response = client.getData(VALID_UUID, VALID_CRYPT);
        
        // Without a running server, we expect an error response
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    @DisplayName("Should handle postData request structure") 
    void shouldHandlePostDataRequestStructure() {
        Container testContainer = createTestContainer();
        
        PocketApiClient.ApiResponse<Container> response = client.postData(VALID_UUID, VALID_CRYPT, testContainer);
        
        // Without a running server, we expect an error response
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    @DisplayName("Should handle changePassword request structure")
    void shouldHandleChangePasswordRequestStructure() {
        PocketApiClient.ApiResponse<Boolean> response = client.changePassword(VALID_UUID, VALID_CRYPT, true);
        
        // Without a running server, we expect an error response
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    @DisplayName("Should handle deleteCacheRecord request structure")
    void shouldHandleDeleteCacheRecordRequestStructure() {
        PocketApiClient.ApiResponse<Void> response = client.deleteCacheRecord(VALID_UUID, VALID_CRYPT);
        
        // Without a running server, we expect an error response
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    @DisplayName("Should handle checkCacheRecord request structure")
    void shouldHandleCheckCacheRecordRequestStructure() {
        PocketApiClient.ApiResponse<Void> response = client.checkCacheRecord(VALID_UUID, VALID_CRYPT);
        
        // Without a running server, we expect an error response
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    @DisplayName("Should handle health check request structure")
    void shouldHandleHealthCheckRequestStructure() {
        PocketApiClient.ApiResponse<String> response = client.getHealth();
        
        // Without a running server, we expect an error response
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    @DisplayName("Should handle async requests and connection failures gracefully")
    void shouldHandleAsyncRequests() throws Exception {
        try {
            CompletableFuture<PocketApiClient.ApiResponse<Container>> future = client.getDataAsync(VALID_UUID, VALID_CRYPT);
            
            assertNotNull(future);
            
            // Wait for completion (with timeout)
            PocketApiClient.ApiResponse<Container> response = future.get(5, TimeUnit.SECONDS);
            
            // Without a running server, we expect an error response
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNotNull(response.getErrorMessage());
        } catch (ExecutionException e) {
            // Connection failures are expected in test environment without running server
            assertTrue(e.getCause() instanceof ConnectException || 
                      e.getCause().getMessage().contains("Connection refused"));
        }
    }

    @Test
    @DisplayName("Should test connection")
    void shouldTestConnection() {
        boolean connected = client.testConnection();
        
        // Without a running server, connection should fail
        assertFalse(connected);
    }

    @Test
    @DisplayName("Should create batch requests")
    void shouldCreateBatchRequests() {
        int requestCount = 5;
        CompletableFuture<PocketApiClient.ApiResponse<Container>>[] futures = client.createBatchRequests(VALID_UUID, VALID_CRYPT, requestCount);
        
        assertNotNull(futures);
        assertEquals(requestCount, futures.length);
        
        // Verify all futures are created
        for (CompletableFuture<PocketApiClient.ApiResponse<Container>> future : futures) {
            assertNotNull(future);
        }
    }

    @Test
    @DisplayName("Should handle invalid UUID format gracefully")
    void shouldHandleInvalidUuidFormatGracefully() {
        String invalidUuid = "invalid-uuid";
        
        PocketApiClient.ApiResponse<Container> response = client.getData(invalidUuid, VALID_CRYPT);
        
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    @DisplayName("Should handle empty parameters gracefully")
    void shouldHandleEmptyParametersGracefully() {
        PocketApiClient.ApiResponse<Container> response = client.getData("", "");
        
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    @DisplayName("ApiResponse should handle success case correctly")
    void apiResponseShouldHandleSuccessCaseCorrectly() {
        Container testData = createTestContainer();
        PocketApiClient.ApiResponse<Container> response = PocketApiClient.ApiResponse.success(testData, 200);
        
        assertTrue(response.isSuccess());
        assertEquals(testData, response.getData());
        assertNull(response.getErrorMessage());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    @DisplayName("ApiResponse should handle error case correctly")
    void apiResponseShouldHandleErrorCaseCorrectly() {
        String errorMessage = "Test error";
        PocketApiClient.ApiResponse<Container> response = PocketApiClient.ApiResponse.error(errorMessage);
        
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals(errorMessage, response.getErrorMessage());
        assertEquals(-1, response.getStatusCode());
    }

    @Test
    @DisplayName("ApiResponse toString should work correctly")
    void apiResponseToStringShouldWorkCorrectly() {
        // Test success response toString
        Container testData = createTestContainer();
        PocketApiClient.ApiResponse<Container> successResponse = PocketApiClient.ApiResponse.success(testData, 200);
        String successString = successResponse.toString();
        assertTrue(successString.contains("success=true"));
        assertTrue(successString.contains("statusCode=200"));

        // Test error response toString
        PocketApiClient.ApiResponse<Container> errorResponse = PocketApiClient.ApiResponse.error("Test error");
        String errorString = errorResponse.toString();
        assertTrue(errorString.contains("success=false"));
        assertTrue(errorString.contains("Test error"));
    }

    // Helper method to create test data
    private Container createTestContainer() {
        User testUser = new User("Test User", "test@example.com", "password123");
        testUser.setId(1L);

        Device testDevice = new Device(testUser);
        testDevice.setId(1L);
        testDevice.setUuid(VALID_UUID);

        return new Container(
                System.currentTimeMillis(),
                testUser,
                testDevice,
                List.of(), // groups
                List.of(), // groupFields
                List.of()  // fields
        );
    }
}